package im.mak.paddle;

import com.wavesplatform.wavesj.ByteString;
import com.wavesplatform.wavesj.DataEntry;
import com.wavesplatform.wavesj.PrivateKeyAccount;
import com.wavesplatform.wavesj.matcher.Order;
import com.wavesplatform.wavesj.matcher.OrderV2;
import com.wavesplatform.wavesj.transactions.*;
import im.mak.paddle.actions.*;
import im.mak.paddle.api.deser.ScriptInfo;
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
import static im.mak.paddle.actions.IssueNft.issueNft;
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

    public ScriptInfo scriptInfo() {
        return node.api.scriptInfo(address());
    }

    public boolean isSmart() {
        return node.isSmart(this);
    }

    public long balance() {
        return node.balance(address());
    }

    public long balance(String assetId) {
        return node.balance(address(), assetId);
    }

    public List<DataEntry> data() {
        return node.data(address());
    }

    public DataEntry dataByKey(String key) {
        return node.dataByKey(address(), key);
    }

    public String dataStr(String key) {
        return (String) dataByKey(key).getValue();
    }

    public long dataInt(String key) {
        return (long) dataByKey(key).getValue();
    }

    public boolean dataBool(String key) {
        return (boolean) dataByKey(key).getValue();
    }

    public byte[] dataBin(String key) {
        return ((ByteString) dataByKey(key).getValue()).getBytes();
    }

    public String sign(byte[] bytes) {
        return wavesAccount.sign(bytes);
    }

    public IssueTransaction issues(Consumer<Issue> i) {
        Issue issue = issue(this);
        i.accept(issue);

        return node.send(issue);
    }

    public IssueTransaction issuesNft(Consumer<IssueNft> i) {
        IssueNft nft = issueNft(this);
        i.accept(nft);
        return issues(a -> {
            a.name(nft.name).description(nft.description).quantity(nft.quantity)
                    .decimals(nft.decimals).reissuable(nft.isReissuable).fee(nft.calcFee());
            a.compiledScript = nft.compiledScript;
        });
    }

    public TransferTransaction transfers(Consumer<Transfer> t) {
        Transfer tr = transfer(this);
        t.accept(tr);

        return node.send(tr);
    }

    public ReissueTransaction reissues(Consumer<Reissue> r) {
        Reissue ri = reissue(this);
        r.accept(ri);

        return node.send(ri);
    }

    public BurnTransaction burns(Consumer<Burn> b) {
        Burn bu = burn(this);
        b.accept(bu);

        return node.send(bu);
    }

    public ExchangeTransaction exchanges(Consumer<Exchange> e) {
        Exchange ex = exchange(this);
        e.accept(ex);

        return node.send(ex);
    }

    public LeaseTransaction leases(Consumer<Lease> lease) {
        Lease l = lease(this);
        lease.accept(l);

        return node.send(l);
    }

    public LeaseCancelTransaction cancelsLease(Consumer<LeaseCancel> l) {
        LeaseCancel lc = leaseCancel(this);
        l.accept(lc);

        return node.send(lc);
    }

    public AliasTransaction createsAlias(Consumer<CreateAlias> a) {
        CreateAlias ca = createAlias(this);
        a.accept(ca);

        return node.send(ca);
    }

    public MassTransferTransaction massTransfers(Consumer<MassTransfer> m) {
        MassTransfer mt = massTransfer(this);
        m.accept(mt);

        return node.send(mt);
    }

    public DataTransaction writes(Consumer<WriteData> d) {
        WriteData wd = writeData(this);
        d.accept(wd);

        return node.send(wd);
    }

    public SetScriptTransaction setsScript(Consumer<SetScript> s) {
        SetScript ss = setScript(this);
        s.accept(ss);

        return node.send(ss);
    }

    public SponsorTransaction sponsors(Consumer<SponsorFee> s) {
        SponsorFee sf = sponsorFee(this);
        s.accept(sf);

        return node.send(sf);
    }

    public SetAssetScriptTransaction setsAssetScript(Consumer<SetAssetScript> s) {
        SetAssetScript sa = setAssetScript(this);
        s.accept(sa);

        return node.send(sa);
    }

    public InvokeScriptTransaction invokes(Consumer<InvokeScript> i) {
        InvokeScript is = invokeScript(this);
        i.accept(is);

        return node.send(is);
    }
}
