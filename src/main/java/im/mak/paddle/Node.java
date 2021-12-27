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
import com.wavesplatform.wavesj.info.TransactionInfo;
import im.mak.paddle.exceptions.ApiError;
import im.mak.paddle.exceptions.NodeError;
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

    protected final Settings conf;
    protected final Account faucet;

    public Node(Settings settings) throws NodeException, IOException, URISyntaxException {
        super(maybeRunDockerContainer(settings));
        conf = settings;
        faucet = conf.faucetSeed == null ? null : new Account(PrivateKey.fromSeed(conf().faucetSeed), this);
    }

    protected Node() throws NodeException, IOException, URISyntaxException {
        this(new Settings());
    }

    protected static String maybeRunDockerContainer(Settings conf) {
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

    public Settings conf() {
        return conf;
    }

    public Account faucet() {
        return faucet;
    }

    public int minAssetInfoUpdateInterval() {
        return conf().minAssetInfoUpdateInterval;
    }

    protected <T> T throwErrorOrGet(Callable<T> mightThrowException) {
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
    public int getBlockHeight(long timestamp) {
        return throwErrorOrGet(() -> super.getBlockHeight(timestamp));
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
    public <T extends Transaction> Validation validateTransaction(T transaction) {
        return throwErrorOrGet(() -> super.validateTransaction(transaction));
    }

    @Override
    public List<LeaseInfo> getActiveLeases(Address address) {
        return throwErrorOrGet(() -> super.getActiveLeases(address));
    }

    @Override
    public LeaseInfo getLeaseInfo(Id leaseId) {
        return throwErrorOrGet(() -> super.getLeaseInfo(leaseId));
    }

    @Override
    public List<LeaseInfo> getLeasesInfo(List<Id> leaseIds) {
        return throwErrorOrGet(() -> super.getLeasesInfo(leaseIds));
    }

    @Override
    public List<LeaseInfo> getLeasesInfo(Id... leaseIds) {
        return throwErrorOrGet(() -> super.getLeasesInfo(leaseIds));
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
    public <T extends TransactionInfo> T getTransactionInfo(Id txId, Class<T> transactionInfoClass) {
        return throwErrorOrGet(() -> super.getTransactionInfo(txId, transactionInfoClass));
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

    //@Override
    public ScriptInfo compileScript(String source, boolean enableCompaction) {
        return throwErrorOrGet(() -> super.compileScript(source, enableCompaction));
    }

    @Override
    public TransactionInfo waitForTransaction(Id id, int waitingInSeconds) {
        return throwErrorOrGet(() -> super.waitForTransaction(id, waitingInSeconds));
    }

    @Override
    public TransactionInfo waitForTransaction(Id id) {
        return throwErrorOrGet(() -> super.waitForTransaction(id));
    }

    @Override
    public <T extends TransactionInfo> T waitForTransaction(Id id, Class<T> infoClass) {
        return throwErrorOrGet(() -> super.waitForTransaction(id, infoClass));
    }

    @Override
    public void waitForTransactions(List<Id> ids, int waitingInSeconds) {
        throwErrorOrGet(() -> {
            super.waitForTransactions(ids, waitingInSeconds);
            return true;
        });
    }

    @Override
    public void waitForTransactions(List<Id> ids) {
        throwErrorOrGet(() -> {
            super.waitForTransactions(ids);
            return true;
        });
    }

    @Override
    public void waitForTransactions(Id... ids) {
        throwErrorOrGet(() -> {
            super.waitForTransactions(ids);
            return true;
        });
    }

    @Override
    public int waitForHeight(int target, int waitingInSeconds) {
        return throwErrorOrGet(() -> super.waitForHeight(target, waitingInSeconds));
    }

    @Override
    public int waitForHeight(int expectedHeight) {
        return throwErrorOrGet(() -> super.waitForHeight(expectedHeight));
    }

    @Override
    public int waitBlocks(int blocksCount, int waitingInSeconds) {
        return throwErrorOrGet(() -> super.waitBlocks(blocksCount, waitingInSeconds));
    }

    @Override
    public int waitBlocks(int blocksCount) {
        return throwErrorOrGet(() -> super.waitBlocks(blocksCount));
    }

}
