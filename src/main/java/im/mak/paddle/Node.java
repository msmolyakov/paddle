package im.mak.paddle;

import com.wavesplatform.wavesj.DataEntry;
import com.wavesplatform.wavesj.Transaction;
import com.wavesplatform.wavesj.matcher.Order;
import com.wavesplatform.wavesj.matcher.OrderV2;
import com.wavesplatform.wavesj.transactions.*;
import im.mak.paddle.actions.*;
import im.mak.paddle.api.Api;
import im.mak.paddle.exceptions.NodeError;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;

import static im.mak.paddle.actions.exchange.OrderType.BUY;
import static im.mak.paddle.actions.exchange.OrderType.SELL;

public class Node {

    private com.wavesplatform.wavesj.Node wavesNode;
    public Account rich;

    public Api api;

    //TODO separate with docker node
    public Node(String uri, char chainId, String richSeed) {
        try {
            this.wavesNode = new com.wavesplatform.wavesj.Node(uri, chainId);

            this.api = new Api(this.wavesNode.getUri());

            this.rich = new Account(richSeed, this);
        } catch (URISyntaxException e) {
            throw new NodeError(e);
        }
    }

    public String version() {
        try {
            return wavesNode.getVersion();
        } catch (IOException e) {
            throw new NodeError(e);
        }
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

    public Transaction waitForTransaction(String id) {
        for (int repeat = 0; repeat < 100; repeat++) {
            try {
                return wavesNode.getTransaction(id);
            } catch (IOException e) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ignored) {}
            }
        }
        throw new NodeError("Could not wait for transaction " + id + " in 10 seconds");
    }

    public int waitForHeight(int target, long durationInMilliseconds) {
        int current = 0;
        int pollingInterval = 1000;
        for (int timeSpent = 0; timeSpent < durationInMilliseconds; timeSpent += pollingInterval) {
            try {
                current = height();
                if (current >= target)
                    return current;
            } catch (NodeError ignored) {}

            if (timeSpent + pollingInterval < durationInMilliseconds)
                try {
                    Thread.sleep(pollingInterval);
                } catch (InterruptedException ignored) {}
        }
        throw new NodeError("Could not wait for height " + target + " in " + (durationInMilliseconds/1000) +
                " seconds. Current height: " + current);
    }

    public int waitForHeight(int expectedHeight) {
        return waitForHeight(expectedHeight, 60_000);
    }

    public int waitNBlocks(int blocksCount, long durationInMilliseconds) {
        return waitForHeight(height() + blocksCount, durationInMilliseconds);
    }

    public int waitNBlocks(int blocksCount) {
        return waitNBlocks(blocksCount, 60_000);
    }

}
