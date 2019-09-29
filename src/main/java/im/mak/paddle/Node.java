package im.mak.paddle;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerCreation;
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
import im.mak.paddle.settings.Env;
import im.mak.paddle.settings.PaddleSettings;
import im.mak.paddle.settings.NodeOptions;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;

import static im.mak.paddle.actions.exchange.OrderType.BUY;
import static im.mak.paddle.actions.exchange.OrderType.SELL;

public class Node {

    private NodeOptions options;
    private com.wavesplatform.wavesj.Node wavesNode;

    public Account rich;

    public Api api;

    public Node(NodeOptions options) {
        this.options = options;
        Env env = new PaddleSettings().env;
        //TODO move to PaddleSettings or Env constructor? And change this.options to read-only this.env?
        if (options.apiUrl == null) options.uri(env.apiUrl());
        if (options.chainId == null) options.chainId(env.chainId());
        if (options.blockInterval < 0) options.blockInterval(env.blockInterval());
        if (options.faucetSeed == null) options.faucetSeed(env.faucetSeed());
        if (options.dockerImage == null) options.dockerImage(env.dockerImage());
        if (options.autoShutdown == null) options.autoShutdown(env.autoShutdown());

        try {
            //TODO apiUrl.toString() ?
            this.wavesNode = new com.wavesplatform.wavesj.Node(options.apiUrl.toString(), options.chainId);

            this.api = new Api(this.wavesNode.getUri());

            this.rich = new Account(options.faucetSeed, this);
        } catch (URISyntaxException e) {
            throw new NodeError(e);
        }

        if (options.dockerImage != null) {
            try {
                //TODO if port is already used - try to connect, otherwise error

                DockerClient docker = DefaultDockerClient.fromEnv().build();
                if (docker.listImages(DockerClient.ListImagesParam.byName(options.dockerImage)).size() < 1) {
                    docker.pull(options.dockerImage);
                } else {
                    //TODO if remote Hub is available, local image exists and image hashes are different then pull again
                }

                Map<String, List<PortBinding>> portBindings = new HashMap<>();
                List<PortBinding> hostPorts = new ArrayList<>();
                hostPorts.add(PortBinding.of("0.0.0.0", options.apiUrl.getPort())); //TODO is 80 if not specified?
                portBindings.put("6869", hostPorts);

                HostConfig hostConfig = HostConfig.builder().portBindings(portBindings).build();

                ContainerConfig containerConfig = ContainerConfig.builder()
                        .hostConfig(hostConfig)
                        .image(options.dockerImage)
                        .exposedPorts("6869")
                        .build();

                ContainerCreation container = this.docker.createContainer(containerConfig);
                this.containerId = container.id();

                this.docker.startContainer(this.containerId);

                //wait node readiness
                boolean isNodeReady = false;
                for (int repeat = 0; repeat < 60; repeat++) {
                    try {
                        api.version();
                        isNodeReady = true;
                        break;
                    } catch (NodeError e) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException ignore) {}
                    }
                }
                if (!isNodeReady) throw new NodeError("Could not wait for node readiness");
            } catch (DockerException | DockerCertificateException | InterruptedException e) {
                throw new NodeError(e);
            }

            if (options.autoShutdown)
                Runtime.getRuntime().addShutdownHook(new Thread() {
                    //TODO if shutdown enabled - create task for shutdown
                });
        }
    }

    public Node() {
        this(NodeOptions.options());
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
            return (IssueTransaction) waitForTransaction(wavesNode.issueAsset(issue.sender.wavesAccount,
                    this.chainId(), issue.name, issue.description, issue.quantity, issue.decimals,
                    issue.isReissuable, issue.compiledScript, issue.calcFee()));
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
            return (InvokeScriptTransaction) waitForTransaction(wavesNode.invokeScript(
                    invoke.sender.wavesAccount, invoke.sender.node.chainId(),
                    invoke.dApp, invoke.call, invoke.payments, invoke.calcFee(), invoke.feeAssetId));
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
        return waitForTransaction(id, (int)(options.blockInterval / 1000));
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
        return waitForHeight(expectedHeight, (int)(options.blockInterval * 3 / 1000));
    }

    public int waitNBlocks(int blocksCount, int blockWaitingInSeconds) {
        if (blockWaitingInSeconds < 1)
            throw new NodeError("waitNBlocks: waiting value must be positive. Current: " + blockWaitingInSeconds);
        return waitForHeight(height() + blocksCount, blockWaitingInSeconds);
    }

    public int waitNBlocks(int blocksCount) {
        return waitNBlocks(blocksCount, (int)(options.blockInterval * 3 / 1000));
    }

}
