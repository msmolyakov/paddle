package im.mak.paddle;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.HostConfig;
import com.spotify.docker.client.messages.PortBinding;
import com.wavesplatform.wavesj.DataEntry;
import com.wavesplatform.wavesj.Transaction;
import com.wavesplatform.wavesj.matcher.Order;
import com.wavesplatform.wavesj.matcher.OrderV2;
import com.wavesplatform.wavesj.transactions.*;
import im.mak.paddle.actions.*;
import im.mak.paddle.api.Api;
import im.mak.paddle.exceptions.NodeError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

import static im.mak.paddle.actions.exchange.OrderType.BUY;
import static im.mak.paddle.actions.exchange.OrderType.SELL;
import static java.util.Collections.singletonList;

@SuppressWarnings("WeakerAccess")
public class Node {

    private static Node instance;

    public static Node node() {
        if (instance == null) synchronized (Node.class) {
            if (instance == null) instance = new Node();
        }
        return instance;
    }

    private final Settings conf;
    private final com.wavesplatform.wavesj.Node wavesNode;
    private Account faucet;

    public Api api;

    static final Logger log = LoggerFactory.getLogger(Node.class);

    private Node() {
        conf = new Settings();
        log.info("Paddle settings: {\n\tprofile: \"{}\",\n\tapiUrl: \"{}\",\n\tchainId: \"{}\",\n\tblockInterval: {}," +
                        "\n\tdockerImage: \"{}\",\n\tautoShutdown: {} }",
                conf.name, conf.apiUrl, conf.chainId, conf.blockInterval, conf.dockerImage, conf.autoShutdown);

        try {
            this.wavesNode = new com.wavesplatform.wavesj.Node(conf.apiUrl, conf.chainId);
            this.api = new Api(this.wavesNode.getUri());
        } catch (URISyntaxException e) {
            throw new NodeError(e);
        }

        if (conf.dockerImage != null) {
            log.info("Starting the node from docker image");
            DockerClient docker;
            String containerId;
            try {
                docker = DefaultDockerClient.fromEnv().build();
                try {
                    docker.pull(conf.dockerImage);
                } catch (DockerException | InterruptedException ignore) {}

                URL apiUrl = new URL(conf.apiUrl);
                int port = apiUrl.getPort() < 0 ? 80 : apiUrl.getPort();
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
                        api.version();
                        isNodeReady = true;
                        break;
                    } catch (NodeError e) {
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
                        if (docker != null
                                && docker.listContainers().stream().anyMatch(c -> c.id().equals(containerId))) {
                            docker.killContainer(containerId);
                            docker.removeContainer(containerId);
                            docker.close();
                        }
                    } catch (DockerException | InterruptedException e) { e.printStackTrace(); }
                }));

            log.info("Docker container id: {}", containerId);
        }
    }

    public Account faucet() {
        if (faucet == null) faucet = new Account(conf.faucetSeed);
        return faucet;
    }

    public byte chainId() {
        return wavesNode.getChainId();
    }

    public int height() {
        try {
            return wavesNode.getHeight();
        } catch (IOException e) {
            throw new NodeError(e);
        }
    }

    public long balance(String address) {
        try {
            return wavesNode.getBalance(address);
        } catch (IOException e) {
            throw new NodeError(e);
        }
    }

    public long balance(String address, String assetId) {
        try {
            return wavesNode.getBalance(address, assetId);
        } catch (IOException e) {
            throw new NodeError(e);
        }
    }

    public List<DataEntry> data(String address) {
        try {
            return wavesNode.getData(address);
        } catch (IOException e) {
            throw new NodeError(e);
        }
    }

    public DataEntry dataByKey(String address, String key) {
        try {
            return wavesNode.getDataByKey(address, key);
        } catch (IOException e) {
            throw new NodeError(e);
        }
    }

    public boolean isSmart(String assetIdOrAddress) {
        if (assetIdOrAddress == null || assetIdOrAddress.isEmpty() || "WAVES".equals(assetIdOrAddress))
            return false;
        else if (assetIdOrAddress.length() > 40) {
            return api.assetDetails(assetIdOrAddress).scripted;
        } else {
            return api.scriptInfo(assetIdOrAddress).extraFee > 0;
        }
    }

    public boolean isSmart(Account account) {
        return isSmart(account.address());
    }

    public String compileScript(String s) {
        try {
            return wavesNode.compileScript(s);
        } catch (IOException e) {
            throw new NodeError(e);
        }
    }

    public IssueTransaction send(Issue issue) {
        try {
            IssueTransaction tx = (IssueTransaction) waitForTransaction(wavesNode.issueAsset(issue.sender.wavesAccount,
                    this.chainId(), issue.name, issue.description, issue.quantity, issue.decimals,
                    issue.isReissuable, issue.compiledScript, issue.calcFee()));
            return tx;
        } catch (IOException e) {
            throw new NodeError(e);
        }
    }

    public TransferTransaction send(Transfer transfer) {
        try {
            return (TransferTransaction) waitForTransaction(wavesNode.transfer(transfer.sender.wavesAccount,
                    transfer.recipient, transfer.amount, transfer.assetId,
                    transfer.calcFee(), transfer.feeAssetId, transfer.attachment));
        } catch (IOException e) {
            throw new NodeError(e);
        }
    }

    public ReissueTransaction send(Reissue reissue) {
        try {
            return (ReissueTransaction) waitForTransaction(wavesNode.reissueAsset(reissue.sender.wavesAccount,
                    this.chainId(), reissue.assetId, reissue.quantity, reissue.isReissuable, reissue.calcFee()));
        } catch (IOException e) {
            throw new NodeError(e);
        }
    }

    public BurnTransaction send(Burn burn) {
        try {
            return (BurnTransaction) waitForTransaction(wavesNode.burnAsset(
                    burn.sender.wavesAccount, this.chainId(), burn.assetId, burn.quantity, burn.calcFee()));
        } catch (IOException e) {
            throw new NodeError(e);
        }
    }

    public ExchangeTransaction send(Exchange exchange) {
        long now = System.currentTimeMillis();
        long nowPlus29Days = now + 2505600000L; //TODO move to Order as default

        OrderV2 buyV2 = new OrderV2(exchange.buy.sender.wavesAccount, exchange.buy.matcher.wavesAccount,
                exchange.buy.type == BUY ? Order.Type.BUY : Order.Type.SELL,
                exchange.buy.pair, exchange.buy.amount, exchange.buy.price,
                now, nowPlus29Days, exchange.buy.calcMatcherFee(), com.wavesplatform.wavesj.matcher.Order.V2);
        OrderV2 sellV2 = new OrderV2(exchange.sell.sender.wavesAccount, exchange.sell.matcher.wavesAccount,
                exchange.sell.type == SELL ? Order.Type.SELL : Order.Type.BUY,
                exchange.sell.pair, exchange.sell.amount, exchange.sell.price,
                now, nowPlus29Days, exchange.buy.calcMatcherFee(), com.wavesplatform.wavesj.matcher.Order.V2);
        try {
            return (ExchangeTransaction) waitForTransaction(wavesNode.exchange(exchange.sender.wavesAccount,
                    buyV2, sellV2, exchange.calcAmount(), exchange.calcPrice(),
                    exchange.calcBuyMatcherFee(), exchange.calcSellMatcherFee(), exchange.calcFee()));
        } catch (IOException e) {
            throw new NodeError(e);
        }
    }

    public LeaseTransaction send(Lease lease) {
        try {
            return (LeaseTransaction) waitForTransaction(wavesNode.lease(
                    lease.sender.wavesAccount, lease.recipient, lease.amount, lease.calcFee()));
        } catch (IOException e) {
            throw new NodeError(e);
        }
    }

    public LeaseCancelTransaction send(LeaseCancel cancel) {
        try {
            return (LeaseCancelTransaction) waitForTransaction(wavesNode.cancelLease(
                    cancel.sender.wavesAccount, this.chainId(), cancel.leaseId, cancel.calcFee()));
        } catch (IOException e) {
            throw new NodeError(e);
        }
    }

    public AliasTransaction send(CreateAlias alias) {
        try {
            return (AliasTransaction) waitForTransaction(wavesNode.alias(
                    alias.sender.wavesAccount, this.chainId(), alias.alias, alias.calcFee()));
        } catch (IOException e) {
            throw new NodeError(e);
        }
    }

    public MassTransferTransaction send(MassTransfer mass) {
        List<com.wavesplatform.wavesj.Transfer> transfers = new LinkedList<>();
        mass.transfers.forEach(t -> transfers.add(new com.wavesplatform.wavesj.Transfer(t.recipient, t.amount)));
        try {
            return (MassTransferTransaction) waitForTransaction(wavesNode.massTransfer(
                    mass.sender.wavesAccount, mass.assetId, transfers, mass.calcFee(), mass.attachment));
        } catch (IOException e) {
            throw new NodeError(e);
        }
    }

    public DataTransaction send(WriteData data) {
        try {
            return (DataTransaction) waitForTransaction(wavesNode.data(
                    data.sender.wavesAccount, data.data, data.calcFee()));
        } catch (IOException e) {
            throw new NodeError(e);
        }
    }

    public SetScriptTransaction send(SetScript set) {
        try {
            return (SetScriptTransaction) waitForTransaction(wavesNode.setScript(
                    set.sender.wavesAccount, set.compiledScript, this.chainId(), set.calcFee()));
        } catch (IOException e) {
            throw new NodeError(e);
        }
    }

    public SponsorTransaction send(SponsorFee sponsor) {
        try {
            return (SponsorTransaction) waitForTransaction(wavesNode.sponsorAsset(
                    sponsor.sender.wavesAccount, sponsor.assetId, sponsor.minSponsoredAssetFee, sponsor.calcFee()));
        } catch (IOException e) {
            throw new NodeError(e);
        }
    }

    public SetAssetScriptTransaction send(SetAssetScript set) {
        try {
            return (SetAssetScriptTransaction) waitForTransaction(wavesNode.setAssetScript(
                    set.sender.wavesAccount, this.chainId(), set.assetId, set.compiledScript, set.calcFee()));
        } catch (IOException e) {
            throw new NodeError(e);
        }
    }

    public InvokeScriptTransaction send(InvokeScript invoke) {
        try {
            InvokeScriptTransaction tx = (InvokeScriptTransaction) waitForTransaction(wavesNode.invokeScript(
                    invoke.sender.wavesAccount, this.chainId(),
                    invoke.dApp, invoke.call, invoke.payments, invoke.calcFee(), invoke.feeAssetId));
            log.info("{} called function \"{}\" at dApp {}", invoke.sender.address(), invoke.call.getName(), invoke.dApp);
            return tx;
        } catch (IOException e) {
            throw new NodeError(e);
        }
    }

    public Transaction waitForTransaction(String id, int waitingInSeconds) {
        int pollingIntervalInMillis = 100;

        if (waitingInSeconds < 1)
            throw new NodeError("waitForTransaction: waiting value must be positive. Current: " + waitingInSeconds);

        for (long spentMillis = 0; spentMillis < waitingInSeconds * 1000L; spentMillis += pollingIntervalInMillis) {
            try {
                return wavesNode.getTransaction(id);
            } catch (IOException e) {
                try {
                    Thread.sleep(pollingIntervalInMillis);
                } catch (InterruptedException ignored) {}
            }
        }
        throw new NodeError("Could not wait for transaction " + id + " in " + waitingInSeconds + " seconds");
    }

    public Transaction waitForTransaction(String id) {
        return waitForTransaction(id, (int)(conf.blockInterval / 1000));
    }

    public int waitForHeight(int target, int blockWaitingInSeconds) {
        int start = height();
        int prev = start;
        int pollingIntervalInMillis = 100;

        if (blockWaitingInSeconds < 1)
            throw new NodeError("waitForHeight: waiting value must be positive. Current: " + blockWaitingInSeconds);

        for (long spentMillis = 0; spentMillis < blockWaitingInSeconds * 1000L; spentMillis += pollingIntervalInMillis) {
            try {
                int current = height();

                if (current >= target)
                    return current;
                else if (current > prev) {
                    prev = current;
                    spentMillis = 0;
                }
            } catch (NodeError ignored) {}

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
        return waitForHeight(height() + blocksCount, blockWaitingInSeconds);
    }

    public int waitNBlocks(int blocksCount) {
        return waitNBlocks(blocksCount, (int)(conf.blockInterval * 3 / 1000));
    }

}
