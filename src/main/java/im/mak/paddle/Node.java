package im.mak.paddle;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.HostConfig;
import com.spotify.docker.client.messages.PortBinding;
import com.wavesplatform.wavesj.*;
import com.wavesplatform.wavesj.exceptions.NodeException;
import im.mak.paddle.api.TxDebugInfo;
import im.mak.paddle.api.TxInfo;
import im.mak.paddle.exceptions.ApiError;
import im.mak.paddle.exceptions.NodeError;
import im.mak.waves.transactions.LeaseTransaction;
import im.mak.waves.transactions.Transaction;
import im.mak.waves.transactions.account.Address;
import im.mak.waves.transactions.common.*;
import im.mak.waves.transactions.data.DataEntry;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
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

    public Account faucet() {
        if (faucet == null) faucet = new Account(conf.faucetSeed);
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

    @Override
    public TxDebugInfo getStateChanges(Id txId) {
        try {
            TransactionDebugInfo info = super.getStateChanges(txId);
            return new TxDebugInfo(info.tx(), info.applicationStatus(), info.height(), info.stateChanges());
        } catch (IOException e) {
            throw new NodeError(e);
        } catch (NodeException e) {
            throw new ApiError(e.getErrorCode(), e.getMessage());
        }
    }

    public <T extends Transaction> T getUnconfirmedTransaction(Id txId, Class<T> txClass) {
        try {
            Transaction tx = super.getUnconfirmedTransaction(txId);
            return (T) tx;
        } catch (IOException e) {
            throw new NodeError(e);
        } catch (NodeException e) {
            throw new ApiError(e.getErrorCode(), e.getMessage());
        }
    }

    @Override
    public List<Address> getAddresses() {
        try {
            return super.getAddresses();
        } catch (IOException e) {
            throw new NodeError(e);
        } catch (NodeException e) {
            throw new ApiError(e.getErrorCode(), e.getMessage());
        }
    }

    @Override
    public List<Address> getAddresses(int fromIndex, int toIndex) {
        try {
            return super.getAddresses(fromIndex, toIndex);
        } catch (IOException e) {
            throw new NodeError(e);
        } catch (NodeException e) {
            throw new ApiError(e.getErrorCode(), e.getMessage());
        }
    }

    @Override
    public long getBalance(Address address) {
        try {
            return super.getBalance(address);
        } catch (IOException e) {
            throw new NodeError(e);
        } catch (NodeException e) {
            throw new ApiError(e.getErrorCode(), e.getMessage());
        }
    }

    @Override
    public long getBalance(Address address, int confirmations) {
        try {
            return super.getBalance(address, confirmations);
        } catch (IOException e) {
            throw new NodeError(e);
        } catch (NodeException e) {
            throw new ApiError(e.getErrorCode(), e.getMessage());
        }
    }

    @Override
    public BalanceDetails getBalanceDetails(Address address) {
        try {
            return super.getBalanceDetails(address);
        } catch (IOException e) {
            throw new NodeError(e);
        } catch (NodeException e) {
            throw new ApiError(e.getErrorCode(), e.getMessage());
        }
    }

    @Override
    public List<DataEntry> getData(Address address) {
        try {
            return super.getData(address);
        } catch (IOException e) {
            throw new NodeError(e);
        } catch (NodeException e) {
            throw new ApiError(e.getErrorCode(), e.getMessage());
        }
    }

    @Override
    public List<DataEntry> getData(Address address, List<String> keys) {
        try {
            return super.getData(address, keys);
        } catch (IOException e) {
            throw new NodeError(e);
        } catch (NodeException e) {
            throw new ApiError(e.getErrorCode(), e.getMessage());
        }
    }

    @Override
    public List<DataEntry> getData(Address address, Pattern regex) {
        try {
            return super.getData(address, regex);
        } catch (IOException e) {
            throw new NodeError(e);
        } catch (NodeException e) {
            throw new ApiError(e.getErrorCode(), e.getMessage());
        }
    }

    @Override
    public DataEntry getData(Address address, String key) {
        try {
            return super.getData(address, key);
        } catch (IOException e) {
            throw new NodeError(e);
        } catch (NodeException e) {
            throw new ApiError(e.getErrorCode(), e.getMessage());
        }
    }

    @Override
    public long getEffectiveBalance(Address address) {
        try {
            return super.getEffectiveBalance(address);
        } catch (IOException e) {
            throw new NodeError(e);
        } catch (NodeException e) {
            throw new ApiError(e.getErrorCode(), e.getMessage());
        }
    }

    @Override
    public long getEffectiveBalance(Address address, int confirmations) {
        try {
            return super.getEffectiveBalance(address, confirmations);
        } catch (IOException e) {
            throw new NodeError(e);
        } catch (NodeException e) {
            throw new ApiError(e.getErrorCode(), e.getMessage());
        }
    }

    @Override
    public ScriptInfo getScriptInfo(Address address) {
        try {
            return super.getScriptInfo(address);
        } catch (IOException e) {
            throw new NodeError(e);
        } catch (NodeException e) {
            throw new ApiError(e.getErrorCode(), e.getMessage());
        }
    }

    @Override
    public ScriptMeta getScriptMeta(Address address) {
        try {
            return super.getScriptMeta(address);
        } catch (IOException e) {
            throw new NodeError(e);
        } catch (NodeException e) {
            throw new ApiError(e.getErrorCode(), e.getMessage());
        }
    }

    @Override
    public List<Alias> getAliasesByAddress(Address address) {
        try {
            return super.getAliasesByAddress(address);
        } catch (IOException e) {
            throw new NodeError(e);
        } catch (NodeException e) {
            throw new ApiError(e.getErrorCode(), e.getMessage());
        }
    }

    @Override
    public Address getAddressByAlias(Alias alias) {
        try {
            return super.getAddressByAlias(alias);
        } catch (IOException e) {
            throw new NodeError(e);
        } catch (NodeException e) {
            throw new ApiError(e.getErrorCode(), e.getMessage());
        }
    }

    @Override
    public AssetDistribution getAssetDistribution(AssetId assetId, int height) {
        try {
            return super.getAssetDistribution(assetId, height);
        } catch (IOException e) {
            throw new NodeError(e);
        } catch (NodeException e) {
            throw new ApiError(e.getErrorCode(), e.getMessage());
        }
    }

    @Override
    public AssetDistribution getAssetDistribution(AssetId assetId, int height, int limit) {
        try {
            return super.getAssetDistribution(assetId, height, limit);
        } catch (IOException e) {
            throw new NodeError(e);
        } catch (NodeException e) {
            throw new ApiError(e.getErrorCode(), e.getMessage());
        }
    }

    @Override
    public AssetDistribution getAssetDistribution(AssetId assetId, int height, int limit, Address after) {
        try {
            return super.getAssetDistribution(assetId, height, limit, after);
        } catch (IOException e) {
            throw new NodeError(e);
        } catch (NodeException e) {
            throw new ApiError(e.getErrorCode(), e.getMessage());
        }
    }

    @Override
    public List<AssetBalance> getAssetsBalance(Address address) {
        try {
            return super.getAssetsBalance(address);
        } catch (IOException e) {
            throw new NodeError(e);
        } catch (NodeException e) {
            throw new ApiError(e.getErrorCode(), e.getMessage());
        }
    }

    @Override
    public long getAssetBalance(Address address, AssetId assetId) {
        try {
            return super.getAssetBalance(address, assetId);
        } catch (IOException e) {
            throw new NodeError(e);
        } catch (NodeException e) {
            throw new ApiError(e.getErrorCode(), e.getMessage());
        }
    }

    @Override
    public AssetDetails getAssetDetails(AssetId assetId) {
        try {
            return super.getAssetDetails(assetId);
        } catch (IOException e) {
            throw new NodeError(e);
        } catch (NodeException e) {
            throw new ApiError(e.getErrorCode(), e.getMessage());
        }
    }

    @Override
    public List<AssetDetails> getAssetsDetails(List<AssetId> assetIds) {
        try {
            return super.getAssetsDetails(assetIds);
        } catch (IOException e) {
            throw new NodeError(e);
        } catch (NodeException e) {
            throw new ApiError(e.getErrorCode(), e.getMessage());
        }
    }

    @Override
    public List<AssetDetails> getNft(Address address) {
        try {
            return super.getNft(address);
        } catch (IOException e) {
            throw new NodeError(e);
        } catch (NodeException e) {
            throw new ApiError(e.getErrorCode(), e.getMessage());
        }
    }

    @Override
    public List<AssetDetails> getNft(Address address, int limit) {
        try {
            return super.getNft(address, limit);
        } catch (IOException e) {
            throw new NodeError(e);
        } catch (NodeException e) {
            throw new ApiError(e.getErrorCode(), e.getMessage());
        }
    }

    @Override
    public List<AssetDetails> getNft(Address address, int limit, AssetId after) {
        try {
            return super.getNft(address, limit, after);
        } catch (IOException e) {
            throw new NodeError(e);
        } catch (NodeException e) {
            throw new ApiError(e.getErrorCode(), e.getMessage());
        }
    }

    @Override
    public BlockchainRewards getBlockchainRewards() {
        try {
            return super.getBlockchainRewards();
        } catch (IOException e) {
            throw new NodeError(e);
        } catch (NodeException e) {
            throw new ApiError(e.getErrorCode(), e.getMessage());
        }
    }

    @Override
    public BlockchainRewards getBlockchainRewards(int height) {
        try {
            return super.getBlockchainRewards(height);
        } catch (IOException e) {
            throw new NodeError(e);
        } catch (NodeException e) {
            throw new ApiError(e.getErrorCode(), e.getMessage());
        }
    }

    @Override
    public int getHeight() {
        try {
            return super.getHeight();
        } catch (IOException e) {
            throw new NodeError(e);
        } catch (NodeException e) {
            throw new ApiError(e.getErrorCode(), e.getMessage());
        }
    }

    @Override
    public int getBlockHeight(Base58String blockId) {
        try {
            return super.getBlockHeight(blockId);
        } catch (IOException e) {
            throw new NodeError(e);
        } catch (NodeException e) {
            throw new ApiError(e.getErrorCode(), e.getMessage());
        }
    }

    @Override
    public int getBlocksDelay(Base58String startBlockId, int blocksNum) {
        try {
            return super.getBlocksDelay(startBlockId, blocksNum);
        } catch (IOException e) {
            throw new NodeError(e);
        } catch (NodeException e) {
            throw new ApiError(e.getErrorCode(), e.getMessage());
        }
    }

    @Override
    public BlockHeaders getBlockHeaders(int height) {
        try {
            return super.getBlockHeaders(height);
        } catch (IOException e) {
            throw new NodeError(e);
        } catch (NodeException e) {
            throw new ApiError(e.getErrorCode(), e.getMessage());
        }
    }

    @Override
    public BlockHeaders getBlockHeaders(Base58String blockId) {
        try {
            return super.getBlockHeaders(blockId);
        } catch (IOException e) {
            throw new NodeError(e);
        } catch (NodeException e) {
            throw new ApiError(e.getErrorCode(), e.getMessage());
        }
    }

    @Override
    public List<BlockHeaders> getBlocksHeaders(int fromHeight, int toHeight) {
        try {
            return super.getBlocksHeaders(fromHeight, toHeight);
        } catch (IOException e) {
            throw new NodeError(e);
        } catch (NodeException e) {
            throw new ApiError(e.getErrorCode(), e.getMessage());
        }
    }

    @Override
    public BlockHeaders getLastBlockHeaders() {
        try {
            return super.getLastBlockHeaders();
        } catch (IOException e) {
            throw new NodeError(e);
        } catch (NodeException e) {
            throw new ApiError(e.getErrorCode(), e.getMessage());
        }
    }

    @Override
    public Block getBlock(int height) {
        try {
            return super.getBlock(height);
        } catch (IOException e) {
            throw new NodeError(e);
        } catch (NodeException e) {
            throw new ApiError(e.getErrorCode(), e.getMessage());
        }
    }

    @Override
    public Block getBlock(Base58String blockId) {
        try {
            return super.getBlock(blockId);
        } catch (IOException e) {
            throw new NodeError(e);
        } catch (NodeException e) {
            throw new ApiError(e.getErrorCode(), e.getMessage());
        }
    }

    @Override
    public List<Block> getBlocks(int fromHeight, int toHeight) {
        try {
            return super.getBlocks(fromHeight, toHeight);
        } catch (IOException e) {
            throw new NodeError(e);
        } catch (NodeException e) {
            throw new ApiError(e.getErrorCode(), e.getMessage());
        }
    }

    @Override
    public Block getGenesisBlock() {
        try {
            return super.getGenesisBlock();
        } catch (IOException e) {
            throw new NodeError(e);
        } catch (NodeException e) {
            throw new ApiError(e.getErrorCode(), e.getMessage());
        }
    }

    @Override
    public Block getLastBlock() {
        try {
            return super.getLastBlock();
        } catch (IOException e) {
            throw new NodeError(e);
        } catch (NodeException e) {
            throw new ApiError(e.getErrorCode(), e.getMessage());
        }
    }

    @Override
    public List<Block> getBlocksGeneratedBy(Address generator, int fromHeight, int toHeight) {
        try {
            return super.getBlocksGeneratedBy(generator, fromHeight, toHeight);
        } catch (IOException e) {
            throw new NodeError(e);
        } catch (NodeException e) {
            throw new ApiError(e.getErrorCode(), e.getMessage());
        }
    }

    @Override
    public String getVersion() {
        try {
            return super.getVersion();
        } catch (IOException e) {
            throw new NodeError(e);
        } catch (NodeException e) {
            throw new ApiError(e.getErrorCode(), e.getMessage());
        }
    }

    @Override
    public List<HistoryBalance> getBalanceHistory(Address address) {
        try {
            return super.getBalanceHistory(address);
        } catch (IOException e) {
            throw new NodeError(e);
        } catch (NodeException e) {
            throw new ApiError(e.getErrorCode(), e.getMessage());
        }
    }

    @Override
    public List<TransactionDebugInfo> getStateChangesByAddress(Address address, int limit, Id afterTxId) {
        try {
            return super.getStateChangesByAddress(address, limit, afterTxId);
        } catch (IOException e) {
            throw new NodeError(e);
        } catch (NodeException e) {
            throw new ApiError(e.getErrorCode(), e.getMessage());
        }
    }

    @Override
    public List<TransactionDebugInfo> getStateChangesByAddress(Address address, int limit) {
        try {
            return super.getStateChangesByAddress(address, limit);
        } catch (IOException e) {
            throw new NodeError(e);
        } catch (NodeException e) {
            throw new ApiError(e.getErrorCode(), e.getMessage());
        }
    }

    @Override
    public List<TransactionDebugInfo> getStateChangesByAddress(Address address) {
        try {
            return super.getStateChangesByAddress(address);
        } catch (IOException e) {
            throw new NodeError(e);
        } catch (NodeException e) {
            throw new ApiError(e.getErrorCode(), e.getMessage());
        }
    }

    @Override
    public <T extends Transaction> Validation validateTransaction(T transaction) {
        try {
            return super.validateTransaction(transaction);
        } catch (IOException e) {
            throw new NodeError(e);
        } catch (NodeException e) {
            throw new ApiError(e.getErrorCode(), e.getMessage());
        }
    }

    @Override
    public List<LeaseTransaction> getActiveLeases(Address address) {
        try {
            return super.getActiveLeases(address);
        } catch (IOException e) {
            throw new NodeError(e);
        } catch (NodeException e) {
            throw new ApiError(e.getErrorCode(), e.getMessage());
        }
    }

    @Override
    public <T extends Transaction> Amount calculateTransactionFee(T transaction) {
        try {
            return super.calculateTransactionFee(transaction);
        } catch (IOException e) {
            throw new NodeError(e);
        } catch (NodeException e) {
            throw new ApiError(e.getErrorCode(), e.getMessage());
        }
    }

    @Override
    public <T extends Transaction> T broadcast(T transaction) {
        try {
            return super.broadcast(transaction);
        } catch (IOException e) {
            throw new NodeError(e);
        } catch (NodeException e) {
            throw new ApiError(e.getErrorCode(), e.getMessage());
        }
    }

    @Override
    public TransactionInfo getTransactionInfo(Id txId) {
        try {
            return super.getTransactionInfo(txId);
        } catch (IOException e) {
            throw new NodeError(e);
        } catch (NodeException e) {
            throw new ApiError(e.getErrorCode(), e.getMessage());
        }
    }

    @Override
    public List<TransactionInfo> getTransactionsByAddress(Address address) {
        try {
            return super.getTransactionsByAddress(address);
        } catch (IOException e) {
            throw new NodeError(e);
        } catch (NodeException e) {
            throw new ApiError(e.getErrorCode(), e.getMessage());
        }
    }

    @Override
    public List<TransactionInfo> getTransactionsByAddress(Address address, int limit) {
        try {
            return super.getTransactionsByAddress(address, limit);
        } catch (IOException e) {
            throw new NodeError(e);
        } catch (NodeException e) {
            throw new ApiError(e.getErrorCode(), e.getMessage());
        }
    }

    @Override
    public List<TransactionInfo> getTransactionsByAddress(Address address, int limit, Id afterTxId) {
        try {
            return super.getTransactionsByAddress(address, limit, afterTxId);
        } catch (IOException e) {
            throw new NodeError(e);
        } catch (NodeException e) {
            throw new ApiError(e.getErrorCode(), e.getMessage());
        }
    }

    @Override
    public TransactionStatus getTransactionStatus(Id txId) {
        try {
            return super.getTransactionStatus(txId);
        } catch (IOException e) {
            throw new NodeError(e);
        } catch (NodeException e) {
            throw new ApiError(e.getErrorCode(), e.getMessage());
        }
    }

    @Override
    public List<TransactionStatus> getTransactionsStatus(List<Id> txIds) {
        try {
            return super.getTransactionsStatus(txIds);
        } catch (IOException e) {
            throw new NodeError(e);
        } catch (NodeException e) {
            throw new ApiError(e.getErrorCode(), e.getMessage());
        }
    }

    @Override
    public List<TransactionStatus> getTransactionsStatus(Id... txIds) {
        try {
            return super.getTransactionsStatus(txIds);
        } catch (IOException e) {
            throw new NodeError(e);
        } catch (NodeException e) {
            throw new ApiError(e.getErrorCode(), e.getMessage());
        }
    }

    @Override
    public Transaction getUnconfirmedTransaction(Id txId) {
        try {
            return super.getUnconfirmedTransaction(txId);
        } catch (IOException e) {
            throw new NodeError(e);
        } catch (NodeException e) {
            throw new ApiError(e.getErrorCode(), e.getMessage());
        }
    }

    @Override
    public List<Transaction> getUnconfirmedTransactions() {
        try {
            return super.getUnconfirmedTransactions();
        } catch (IOException e) {
            throw new NodeError(e);
        } catch (NodeException e) {
            throw new ApiError(e.getErrorCode(), e.getMessage());
        }
    }

    @Override
    public int getUtxSize() {
        try {
            return super.getUtxSize();
        } catch (IOException e) {
            throw new NodeError(e);
        } catch (NodeException e) {
            throw new ApiError(e.getErrorCode(), e.getMessage());
        }
    }

    @Override
    public ScriptInfo compileScript(String source) {
        try {
            return super.compileScript(source);
        } catch (IOException e) {
            throw new NodeError(e);
        } catch (NodeException e) {
            throw new ApiError(e.getErrorCode(), e.getMessage());
        }
    }
}
