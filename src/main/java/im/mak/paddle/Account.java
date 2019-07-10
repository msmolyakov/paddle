package im.mak.paddle;

import com.wavesplatform.wavesj.ByteString;
import com.wavesplatform.wavesj.DataEntry;
import com.wavesplatform.wavesj.PrivateKeyAccount;
import com.wavesplatform.wavesj.matcher.Order;
import com.wavesplatform.wavesj.matcher.OrderV2;
import com.wavesplatform.wavesj.transactions.*;
import im.mak.paddle.actions.*;
import im.mak.paddle.exceptions.NodeError;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import static im.mak.paddle.actions.Burn.burn;
import static im.mak.paddle.actions.CreateAlias.createAlias;
import static im.mak.paddle.actions.Exchange.exchange;
import static im.mak.paddle.actions.InvokeScript.invokeScript;
import static im.mak.paddle.actions.Issue.issue;
import static im.mak.paddle.actions.Lease.lease;
import static im.mak.paddle.actions.LeaseCancel.leaseCancel;
import static im.mak.paddle.actions.MassTransfer.massTransfer;
import static im.mak.paddle.actions.Reissue.reissue;
import static im.mak.paddle.actions.SetAssetScript.setAssetScript;
import static im.mak.paddle.actions.SetScript.setScript;
import static im.mak.paddle.actions.SponsorFee.sponsorFee;
import static im.mak.paddle.actions.Transfer.transfer;
import static im.mak.paddle.actions.WriteData.writeData;
import static im.mak.paddle.actions.exchange.OrderType.BUY;
import static im.mak.paddle.actions.exchange.OrderType.SELL;

public class Account {

    public PrivateKeyAccount wavesAccount;
    public final String seedText;
    public Node node;

    public Account(String seedText, Node worksWith, long initWavesBalance) {
        this.seedText = seedText;
        this.node = worksWith;
        wavesAccount = PrivateKeyAccount.fromSeed(this.seedText, 0, node.chainId());

        if (initWavesBalance > 0) {
            this.node.rich.transfers(t -> t.amount(initWavesBalance).to(this));
        }
    }

    public Account(String seedText, Node worksWith) {
        this(seedText, worksWith, 0);
    }

    public Account(Node worksWith, long initWavesBalance) {
        this(UUID.randomUUID().toString(), worksWith, initWavesBalance);
    }

    public Account(Node worksWith) {
        this(worksWith, 0);
    }

    public byte[] privateKey() {
        return this.wavesAccount.getPrivateKey();
    }

    public byte[] publicKey() {
        return this.wavesAccount.getPublicKey();
    }

    public String address() {
        return wavesAccount.getAddress();
    }

    public boolean isSmart() {
        return node.isSmart(this);
    }

    public long balance() {
        try {
            return node.wavesNode.getBalance(address());
        } catch (IOException e) {
            throw new NodeError(e);
        }
    }

    public long balance(String assetId) {
        try {
            return node.wavesNode.getBalance(address(), assetId);
        } catch (IOException e) {
            throw new NodeError(e);
        }
    }

    public List<DataEntry> data() {
        try {
            return node.wavesNode.getData(address());
        } catch (IOException e) {
            throw new NodeError(e);
        }
    }

    public DataEntry data(String key) {
        try {
            return node.wavesNode.getDataByKey(address(), key);
        } catch (IOException e) {
            throw new NodeError(e);
        }
    }

    public String dataStr(String key) {
        return (String) data(key).getValue();
    }

    public long dataInt(String key) {
        return (long) data(key).getValue();
    }

    public boolean dataBool(String key) {
        return (boolean) data(key).getValue();
    }

    public byte[] dataBin(String key) {
        return ((ByteString) data(key).getValue()).getBytes();
    }

    public String sign(byte[] bytes) {
        return wavesAccount.sign(bytes);
    }

    public IssueTransaction issues(Consumer<Issue> i) {
        Issue is = issue(this);
        i.accept(is);

        try {
            return (IssueTransaction) node.waitForTransaction(node.wavesNode.issueAsset(is.sender.wavesAccount,
                    node.chainId(), is.name, is.description, is.quantity, is.decimals,
                    is.isReissuable, is.compiledScript, is.calcFee()));
        } catch (IOException e) {
            throw new NodeError(e);
        }
    }

    public TransferTransaction transfers(Consumer<Transfer> t) {
        Transfer tr = transfer(this);
        t.accept(tr);

        try {
            return (TransferTransaction) node.waitForTransaction(node.wavesNode.transfer(tr.sender.wavesAccount,
                    tr.recipient, tr.amount, tr.assetId, tr.calcFee(), tr.feeAssetId, tr.attachment));
        } catch (IOException e) {
            throw new NodeError(e);
        }
    }

    public ReissueTransaction reissues(Consumer<Reissue> r) {
        Reissue ri = reissue(this);
        r.accept(ri);

        try {
            return (ReissueTransaction) node.waitForTransaction(node.wavesNode.reissueAsset(ri.sender.wavesAccount,
                    node.chainId(), ri.assetId, ri.quantity, ri.isReissuable, ri.calcFee()));
        } catch (IOException e) {
            throw new NodeError(e);
        }
    }

    public BurnTransaction burns(Consumer<Burn> b) {
        Burn bu = burn(this);
        b.accept(bu);

        try {
            return (BurnTransaction) node.waitForTransaction(node.wavesNode.burnAsset(
                    bu.sender.wavesAccount, node.chainId(), bu.assetId, bu.quantity, bu.calcFee()));
        } catch (IOException e) {
            throw new NodeError(e);
        }
    }

    public ExchangeTransaction exchanges(Consumer<Exchange> e) {
        Exchange ex = exchange(this);
        e.accept(ex);

        long now = System.currentTimeMillis();
        long nowPlus29Days = now + 2505600000L;

        OrderV2 buyV2 = new OrderV2(ex.buy.sender.wavesAccount, ex.buy.matcher.wavesAccount,
                ex.buy.type == BUY ? Order.Type.BUY : Order.Type.SELL, ex.buy.pair, ex.buy.amount, ex.buy.price,
                now, nowPlus29Days, ex.buy.calcMatcherFee(), com.wavesplatform.wavesj.matcher.Order.V2);
        OrderV2 sellV2 = new OrderV2(ex.sell.sender.wavesAccount, ex.sell.matcher.wavesAccount,
                ex.sell.type == SELL ? Order.Type.SELL : Order.Type.BUY, ex.sell.pair, ex.sell.amount, ex.sell.price,
                now, nowPlus29Days, ex.buy.calcMatcherFee(), com.wavesplatform.wavesj.matcher.Order.V2);

        try {
            return (ExchangeTransaction) node.waitForTransaction(node.wavesNode.exchange(ex.sender.wavesAccount,
                    buyV2, sellV2, ex.calcAmount(), ex.calcPrice(),
                    ex.calcBuyMatcherFee(), ex.calcSellMatcherFee(), ex.calcFee()));
        } catch (IOException ioe) {
            throw new NodeError(ioe);
        }
    }

    public LeaseTransaction leases(Consumer<Lease> lease) {
        Lease l = lease(this);
        lease.accept(l);

        try {
            return (LeaseTransaction) node.waitForTransaction(node.wavesNode.lease(
                    l.sender.wavesAccount, l.recipient, l.amount, l.calcFee()));
        } catch (IOException e) {
            throw new NodeError(e);
        }
    }

    public LeaseCancelTransaction cancelsLease(Consumer<LeaseCancel> l) {
        LeaseCancel lc = leaseCancel(this);
        l.accept(lc);

        try {
            return (LeaseCancelTransaction) node.waitForTransaction(node.wavesNode.cancelLease(
                    lc.sender.wavesAccount, node.chainId(), lc.leaseId, lc.calcFee()));
        } catch (IOException e) {
            throw new NodeError(e);
        }
    }

    public AliasTransaction createsAlias(Consumer<CreateAlias> a) {
        CreateAlias ca = createAlias(this);
        a.accept(ca);

        try {
            return (AliasTransaction) node.waitForTransaction(node.wavesNode.alias(
                    ca.sender.wavesAccount, node.chainId(), ca.alias, ca.calcFee()));
        } catch (IOException e) {
            throw new NodeError(e);
        }
    }

    public MassTransferTransaction massTransfers(Consumer<MassTransfer> m) {
        MassTransfer mt = massTransfer(this);
        m.accept(mt);

        try {
            List<com.wavesplatform.wavesj.Transfer> transfers = new LinkedList<>();
            mt.transfers.forEach(t -> transfers.add(new com.wavesplatform.wavesj.Transfer(t.recipient, t.amount)));
            return (MassTransferTransaction) node.waitForTransaction(node.wavesNode.massTransfer(
                    mt.sender.wavesAccount, mt.assetId, transfers, mt.calcFee(), mt.attachment));
        } catch (IOException e) {
            throw new NodeError(e);
        }
    }

    public DataTransaction writes(Consumer<WriteData> d) {
        WriteData wd = writeData(this);
        d.accept(wd);

        try {
            return (DataTransaction) node.waitForTransaction(node.wavesNode.data(
                    wd.sender.wavesAccount, wd.data, wd.calcFee()));
        } catch (IOException e) {
            throw new NodeError(e);
        }
    }

    public SetScriptTransaction setsScript(Consumer<SetScript> s) {
        SetScript ss = setScript(this);
        s.accept(ss);

        try {
            return (SetScriptTransaction) node.waitForTransaction(node.wavesNode.setScript(
                    ss.sender.wavesAccount, ss.compiledScript, node.chainId(), ss.calcFee()));
        } catch (IOException e) {
            throw new NodeError(e);
        }
    }

    public SponsorTransaction sponsors(Consumer<SponsorFee> s) {
        SponsorFee sf = sponsorFee(this);
        s.accept(sf);

        try {
            return (SponsorTransaction) node.waitForTransaction(node.wavesNode.sponsorAsset(
                    sf.sender.wavesAccount, sf.assetId, sf.minSponsoredAssetFee, sf.calcFee()));
        } catch (IOException e) {
            throw new NodeError(e);
        }
    }

    public SetAssetScriptTransaction setsAssetScript(Consumer<SetAssetScript> s) {
        SetAssetScript sa = setAssetScript(this);
        s.accept(sa);

        try {
            return (SetAssetScriptTransaction) node.waitForTransaction(node.wavesNode.setAssetScript(
                    sa.sender.wavesAccount, node.chainId(), sa.assetId, sa.compiledScript, sa.calcFee()));
        } catch (IOException e) {
            throw new NodeError(e);
        }
    }

    public InvokeScriptTransaction invokes(Consumer<InvokeScript> i) {
        InvokeScript is = invokeScript(this);
        i.accept(is);

        try {
            return (InvokeScriptTransaction) node.waitForTransaction(node.wavesNode.invokeScript(
                    is.sender.wavesAccount, is.sender.node.chainId(),
                    is.dApp, is.call, is.payments, is.calcFee(), is.feeAssetId));
        } catch (IOException e) {
            throw new NodeError(e);
        }
    }
}
