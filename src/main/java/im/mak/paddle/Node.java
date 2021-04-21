package im.mak.paddle;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.HostConfig;
import com.spotify.docker.client.messages.PortBinding;
import com.wavesplatform.transactions.account.PrivateKey;
import com.wavesplatform.wavesj.*;
import com.wavesplatform.wavesj.exceptions.NodeException;
import im.mak.paddle.api.TxDebugInfo;
import im.mak.paddle.api.TxInfo;
import im.mak.paddle.exceptions.ApiError;
import im.mak.paddle.exceptions.NodeError;
import com.wavesplatform.transactions.LeaseTransaction;
import com.wavesplatform.transactions.Transaction;
import com.wavesplatform.transactions.account.Address;
import com.wavesplatform.transactions.common.*;
import com.wavesplatform.transactions.data.DataEntry;
import im.mak.paddle.internal.Settings;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;

import static java.util.Collections.singletonList;

@SuppressWarnings("WeakerAccess")
public class Node extends com.wavesplatform.wavesj.Node {

    private static Node instance;

    public static Node node() {
        if (instance == null) synchronized (Node.class) {
            if (instance == null) {
                try {
                    instance = new Node();
                } catch (IOException|URISyntaxException e) {
                    throw new NodeError(e);
                } catch (NodeException e) {
                    throw new ApiError(e.getErrorCode(), e.getMessage());
                }
            }
        }
        return instance;
    }

    private final Settings conf;
    private Account faucet;

    private Node() throws NodeException, IOException, URISyntaxException {
        super(maybeRunDockerContainer());
        conf = new Settings();
    }

    private static String maybeRunDockerContainer() {
        Settings conf = new Settings();

        if (conf.dockerImage != null) {
            DockerClient docker;
            String containerId;
            try {
                docker = DefaultDockerClient.fromEnv().build();
                try {
                    docker.pull(conf.dockerImage);
                } catch (DockerException | InterruptedException ignore) {}

                URL apiUrl = new URL(conf.apiUrl);
                int port = apiUrl.getPort() <= 0 ? 80 : apiUrl.getPort();
                Map<String, List<PortBinding>> portBindings = new HashMap<>();
                portBindings.put("6869", singletonList(PortBinding
                        .of("0.0.0.0", port)));
                HostConfig hostConfig = HostConfig.builder().portBindings(portBindings).build();
                ContainerConfig containerConfig = ContainerConfig.builder()
                        .hostConfig(hostConfig)
                        .image(conf.dockerImage)
                        .exposedPorts("6869")
                        .build();

                containerId = docker.createContainer(containerConfig).id();
                docker.startContainer(containerId);

                //wait node readiness
                boolean isNodeReady = false;
                for (int repeat = 0; repeat < 60; repeat++) {
                    try {
                        try {
                            com.wavesplatform.wavesj.Node node = new com.wavesplatform.wavesj.Node(conf.apiUrl);
                        } catch (URISyntaxException|IOException e) {
                            throw new NodeError(e);
                        } catch (NodeException e) {
                            throw new ApiError(e.getErrorCode(), e.getMessage());
                        }
                        isNodeReady = true;
                        break;
                    } catch (NodeError|ApiError e) {
                        try { Thread.sleep(1000); } catch (InterruptedException ignore) {}
                    }
                }
                if (!isNodeReady) throw new NodeError("Could not wait for node readiness");
            } catch (DockerException | DockerCertificateException | InterruptedException | MalformedURLException e) {
                throw new NodeError(e);
            }

            if (conf.autoShutdown)
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    try {
                        if (docker.listContainers().stream().anyMatch(c -> c.id().equals(containerId))) {
                            docker.killContainer(containerId);
                            docker.removeContainer(containerId);
                            docker.close();
                        }
                    } catch (DockerException | InterruptedException e) { e.printStackTrace(); }
                }));
        }
        return conf.apiUrl;
    }

    private static <T> T throwErrorOrGet(Callable<T> mightThrowException) {
        try {
            return mightThrowException.call();
        } catch (IOException e) {
            throw new NodeError(e);
        } catch (NodeException e) {
            throw new ApiError(e.getErrorCode(), e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public int minAssetInfoUpdateInterval() {
        return this.conf.minAssetInfoUpdateInterval;
    }

    public Account faucet() {
        if (faucet == null)
            faucet = new Account(PrivateKey.fromSeed(conf.faucetSeed));
        return faucet;
    }

    public TransactionInfo waitForTransaction(Id id, int waitingInSeconds) {
        int pollingIntervalInMillis = 100;

        if (waitingInSeconds < 1)
            throw new NodeError("waitForTransaction: waiting value must be positive. Current: " + waitingInSeconds);

        for (long spentMillis = 0; spentMillis < waitingInSeconds * 1000L; spentMillis += pollingIntervalInMillis) {
            try {
                return this.getTransactionInfo(id);
            } catch (NodeError|ApiError e) {
                try {
                    Thread.sleep(pollingIntervalInMillis);
                } catch (InterruptedException ignored) {}
            }
        }
        throw new NodeError("Could not wait for transaction " + id + " in " + waitingInSeconds + " seconds");
    }

    //TODO move all waiting functions to WavesJ
    public TransactionInfo waitForTransaction(Id id) {
        return waitForTransaction(id, (int)(conf.blockInterval / 1000));
    }

    public <T extends Transaction> TxInfo<T> waitForTransaction(Id id, Class<T> txClass, int waitingInSeconds) {
        TransactionInfo info = this.waitForTransaction(id, waitingInSeconds);
        return new TxInfo<>(info.tx(), info.applicationStatus(), info.height());
    }

    public <T extends Transaction> TxInfo<T> waitForTransaction(Id id, Class<T> txClass) {
        TransactionInfo info = this.waitForTransaction(id);
        return new TxInfo<>(info.tx(), info.applicationStatus(), info.height());
    }

    public int waitForHeight(int target, int blockWaitingInSeconds) {
        int start = this.getHeight();
        int prev = start;
        int pollingIntervalInMillis = 100;

        if (blockWaitingInSeconds < 1)
            throw new NodeError("waitForHeight: waiting value must be positive. Current: " + blockWaitingInSeconds);

        for (long spentMillis = 0; spentMillis < blockWaitingInSeconds * 1000L; spentMillis += pollingIntervalInMillis) {
            try {
                int current = this.getHeight();

                if (current >= target)
                    return current;
                else if (current > prev) {
                    prev = current;
                    spentMillis = 0;
                }
            } catch (NodeError|ApiError ignored) {}

            try {
                Thread.sleep(pollingIntervalInMillis);
            } catch (InterruptedException ignored) {}
        }
        throw new NodeError("Could not wait for the height to rise from " + start + " to " + target +
                ": height " + prev + " did not grow for " + blockWaitingInSeconds + " seconds");
    }

    public int waitForHeight(int expectedHeight) {
        return waitForHeight(expectedHeight, (int)(conf.blockInterval * 3 / 1000));
    }

    public int waitNBlocks(int blocksCount, int blockWaitingInSeconds) {
        if (blockWaitingInSeconds < 1)
            throw new NodeError("waitNBlocks: waiting value must be positive. Current: " + blockWaitingInSeconds);
        return waitForHeight(this.getHeight() + blocksCount, blockWaitingInSeconds);
    }

    public int waitNBlocks(int blocksCount) {
        return waitNBlocks(blocksCount, (int)(conf.blockInterval * 3 / 1000));
    }

    //TODO move to WavesJ
    public <T extends Transaction> TxInfo<T> getTransactionInfo(Id txId, Class<T> txClass) {
        try {
            TransactionInfo info = super.getTransactionInfo(txId);
            return new TxInfo<>(info.tx(), info.applicationStatus(), info.height());
        } catch (IOException e) {
            throw new NodeError(e);
        } catch (NodeException e) {
            throw new ApiError(e.getErrorCode(), e.getMessage());
        }
    }

    @Override //TODO change to /transactions/info
    public TxDebugInfo getStateChanges(Id txId) {
        return throwErrorOrGet(() -> {
            TransactionDebugInfo info = super.getStateChanges(txId);
            return new TxDebugInfo(info.tx(), info.applicationStatus(), info.height(), info.stateChanges());
        });
    }

    //TODO move to WavesJ
    public <T extends Transaction> T getUnconfirmedTransaction(Id txId, Class<T> txClass) {
        return throwErrorOrGet(() -> {
            Transaction tx = super.getUnconfirmedTransaction(txId);
            return (T) tx;
        });
    }

    @Override
    public List<Address> getAddresses() {
        return throwErrorOrGet(super::getAddresses);
    }

    @Override
    public List<Address> getAddresses(int fromIndex, int toIndex) {
        return throwErrorOrGet(() -> super.getAddresses(fromIndex, toIndex));
    }

    @Override
    public long getBalance(Address address) {
        return throwErrorOrGet(() -> super.getBalance(address));
    }

    @Override
    public long getBalance(Address address, int confirmations) {
        return throwErrorOrGet(() -> super.getBalance(address, confirmations));
    }

    @Override
    public List<Balance> getBalances(List<Address> addresses) {
        return throwErrorOrGet(() -> super.getBalances(addresses));
    }

    @Override
    public List<Balance> getBalances(List<Address> addresses, int height) {
        return throwErrorOrGet(() -> super.getBalances(addresses, height));
    }

    @Override
    public BalanceDetails getBalanceDetails(Address address) {
        return throwErrorOrGet(() -> super.getBalanceDetails(address));
    }

    @Override
    public List<DataEntry> getData(Address address) {
        return throwErrorOrGet(() -> super.getData(address));
    }

    @Override
    public List<DataEntry> getData(Address address, List<String> keys) {
        return throwErrorOrGet(() -> super.getData(address, keys));
    }

    @Override
    public List<DataEntry> getData(Address address, Pattern regex) {
        return throwErrorOrGet(() -> super.getData(address, regex));
    }

    @Override
    public DataEntry getData(Address address, String key) {
        return throwErrorOrGet(() -> super.getData(address, key));
    }

    @Override
    public long getEffectiveBalance(Address address) {
        return throwErrorOrGet(() -> super.getEffectiveBalance(address));
    }

    @Override
    public long getEffectiveBalance(Address address, int confirmations) {
        return throwErrorOrGet(() -> super.getEffectiveBalance(address, confirmations));
    }

    @Override
    public ScriptInfo getScriptInfo(Address address) {
        return throwErrorOrGet(() -> super.getScriptInfo(address));
    }

    @Override
    public ScriptMeta getScriptMeta(Address address) {
        return throwErrorOrGet(() -> super.getScriptMeta(address));
    }

    @Override
    public List<Alias> getAliasesByAddress(Address address) {
        return throwErrorOrGet(() -> super.getAliasesByAddress(address));
    }

    @Override
    public Address getAddressByAlias(Alias alias) {
        return throwErrorOrGet(() -> super.getAddressByAlias(alias));
    }

    @Override
    public AssetDistribution getAssetDistribution(AssetId assetId, int height) {
        return throwErrorOrGet(() -> super.getAssetDistribution(assetId, height));
    }

    @Override
    public AssetDistribution getAssetDistribution(AssetId assetId, int height, int limit) {
        return throwErrorOrGet(() -> super.getAssetDistribution(assetId, height, limit));
    }

    @Override //TODO iterators and parsers for all limited requests
    public AssetDistribution getAssetDistribution(AssetId assetId, int height, int limit, Address after) {
        return throwErrorOrGet(() -> super.getAssetDistribution(assetId, height, limit, after));
    }

    @Override
    public List<AssetBalance> getAssetsBalance(Address address) {
        return throwErrorOrGet(() -> super.getAssetsBalance(address));
    }

    @Override
    public long getAssetBalance(Address address, AssetId assetId) {
        return throwErrorOrGet(() -> super.getAssetBalance(address, assetId));
    }

    @Override
    public AssetDetails getAssetDetails(AssetId assetId) {
        return throwErrorOrGet(() -> super.getAssetDetails(assetId));
    }

    @Override
    public List<AssetDetails> getAssetsDetails(List<AssetId> assetIds) {
        return throwErrorOrGet(() -> super.getAssetsDetails(assetIds));
    }

    @Override
    public List<AssetDetails> getNft(Address address) {
        return throwErrorOrGet(() -> super.getNft(address));
    }

    @Override
    public List<AssetDetails> getNft(Address address, int limit) {
        return throwErrorOrGet(() -> super.getNft(address, limit));
    }

    @Override
    public List<AssetDetails> getNft(Address address, int limit, AssetId after) {
        return throwErrorOrGet(() -> super.getNft(address, limit, after));
    }

    @Override
    public BlockchainRewards getBlockchainRewards() {
        return throwErrorOrGet(super::getBlockchainRewards);
    }

    @Override
    public BlockchainRewards getBlockchainRewards(int height) {
        return throwErrorOrGet(() -> super.getBlockchainRewards(height));
    }

    @Override
    public int getHeight() {
        return throwErrorOrGet(super::getHeight);
    }

    @Override
    public int getBlockHeight(Base58String blockId) {
        return throwErrorOrGet(() -> super.getBlockHeight(blockId));
    }

    @Override
    public int getBlocksDelay(Base58String startBlockId, int blocksNum) {
        return throwErrorOrGet(() -> super.getBlocksDelay(startBlockId, blocksNum));
    }

    @Override
    public BlockHeaders getBlockHeaders(int height) {
        return throwErrorOrGet(() -> super.getBlockHeaders(height));
    }

    @Override
    public BlockHeaders getBlockHeaders(Base58String blockId) {
        return throwErrorOrGet(() -> super.getBlockHeaders(blockId));
    }

    @Override
    public List<BlockHeaders> getBlocksHeaders(int fromHeight, int toHeight) {
        return throwErrorOrGet(() -> super.getBlocksHeaders(fromHeight, toHeight));
    }

    @Override
    public BlockHeaders getLastBlockHeaders() {
        return throwErrorOrGet(super::getLastBlockHeaders);
    }

    @Override
    public Block getBlock(int height) {
        return throwErrorOrGet(() -> super.getBlock(height));
    }

    @Override
    public Block getBlock(Base58String blockId) {
        return throwErrorOrGet(() -> super.getBlock(blockId));
    }

    @Override
    public List<Block> getBlocks(int fromHeight, int toHeight) {
        return throwErrorOrGet(() -> super.getBlocks(fromHeight, toHeight));
    }

    @Override
    public Block getGenesisBlock() {
        return throwErrorOrGet(super::getGenesisBlock);
    }

    @Override
    public Block getLastBlock() {
        return throwErrorOrGet(super::getLastBlock);
    }

    @Override
    public List<Block> getBlocksGeneratedBy(Address generator, int fromHeight, int toHeight) {
        return throwErrorOrGet(() -> super.getBlocksGeneratedBy(generator, fromHeight, toHeight));
    }

    @Override
    public String getVersion() {
        return throwErrorOrGet(super::getVersion);
    }

    @Override
    public List<HistoryBalance> getBalanceHistory(Address address) {
        return throwErrorOrGet(() -> super.getBalanceHistory(address));
    }

    @Override
    public List<TransactionDebugInfo> getStateChangesByAddress(Address address, int limit, Id afterTxId) {
        return throwErrorOrGet(() -> super.getStateChangesByAddress(address, limit, afterTxId));
    }

    @Override
    public List<TransactionDebugInfo> getStateChangesByAddress(Address address, int limit) {
        return throwErrorOrGet(() -> super.getStateChangesByAddress(address, limit));
    }

    @Override
    public List<TransactionDebugInfo> getStateChangesByAddress(Address address) {
        return throwErrorOrGet(() -> super.getStateChangesByAddress(address));
    }

    @Override
    public <T extends Transaction> Validation validateTransaction(T transaction) {
        return throwErrorOrGet(() -> super.validateTransaction(transaction));
    }

    @Override
    public List<LeaseTransaction> getActiveLeases(Address address) {
        return throwErrorOrGet(() -> super.getActiveLeases(address));
    }

    @Override
    public <T extends Transaction> Amount calculateTransactionFee(T transaction) {
        return throwErrorOrGet(() -> super.calculateTransactionFee(transaction));
    }

    @Override
    public <T extends Transaction> T broadcast(T transaction) {
        return throwErrorOrGet(() -> super.broadcast(transaction));
    }

    @Override
    public TransactionInfo getTransactionInfo(Id txId) {
        return throwErrorOrGet(() -> super.getTransactionInfo(txId));
    }

    @Override
    public List<TransactionInfo> getTransactionsByAddress(Address address) {
        return throwErrorOrGet(() -> super.getTransactionsByAddress(address));
    }

    @Override
    public List<TransactionInfo> getTransactionsByAddress(Address address, int limit) {
        return throwErrorOrGet(() -> super.getTransactionsByAddress(address, limit));
    }

    @Override
    public List<TransactionInfo> getTransactionsByAddress(Address address, int limit, Id afterTxId) {
        return throwErrorOrGet(() -> super.getTransactionsByAddress(address, limit, afterTxId));
    }

    @Override
    public TransactionStatus getTransactionStatus(Id txId) {
        return throwErrorOrGet(() -> super.getTransactionStatus(txId));
    }

    @Override
    public List<TransactionStatus> getTransactionsStatus(List<Id> txIds) {
        return throwErrorOrGet(() -> super.getTransactionsStatus(txIds));
    }

    @Override
    public List<TransactionStatus> getTransactionsStatus(Id... txIds) {
        return throwErrorOrGet(() -> super.getTransactionsStatus(txIds));
    }

    @Override
    public Transaction getUnconfirmedTransaction(Id txId) {
        return throwErrorOrGet(() -> super.getUnconfirmedTransaction(txId));
    }

    @Override
    public List<Transaction> getUnconfirmedTransactions() {
        return throwErrorOrGet(super::getUnconfirmedTransactions);
    }

    @Override
    public int getUtxSize() {
        return throwErrorOrGet(super::getUtxSize);
    }

    @Override
    public ScriptInfo compileScript(String source) {
        return throwErrorOrGet(() -> super.compileScript(source));
    }
}
