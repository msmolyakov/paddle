package im.mak.paddle;

import com.wavesplatform.wavesj.ByteString;
import com.wavesplatform.wavesj.DataEntry;
import com.wavesplatform.wavesj.PrivateKeyAccount;
import com.wavesplatform.wavesj.transactions.*;
import im.mak.paddle.actions.*;
import im.mak.paddle.api.deser.transactions.IssueTx;
import im.mak.paddle.api.deser.ScriptInfo;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import static im.mak.paddle.Node.node;

public class Account {

    public PrivateKeyAccount wavesAccount;
    private String seedText;

    public Account(String seedText, long initWavesBalance) {
        this.seedText = seedText;
        wavesAccount = PrivateKeyAccount.fromSeed(this.seedText, 0, node().chainId());

        if (initWavesBalance > 0) {
            node().faucet().transfers(t -> t.amount(initWavesBalance).to(this));
        }
    }

    public Account(String seedText) {
        this(seedText, 0);
    }

    public Account(long initWavesBalance) {
        this(UUID.randomUUID().toString(), initWavesBalance);
    }

    public Account() {
        this(0);
    }

    public String seed() {
        return this.seedText;
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
        return node().api.scriptInfo(address());
    }

    public boolean isSmart() {
        return node().isSmart(this);
    }

    public long balance() {
        return node().balance(address());
    }

    public long balance(String assetId) {
        return node().balance(address(), assetId);
    }

    public List<IssueTx> nft(int limit, String after) {
        return node().api.nft(address(), limit, after);
    }

    public List<IssueTx> nft(int limit) {
        return node().api.nft(address(), limit);
    }

    public List<IssueTx> nft(String after) {
        return node().api.nft(address(), after);
    }

    public List<IssueTx> nft() {
        return node().api.nft(address());
    }

    public List<DataEntry> data() {
        return node().data(address());
    }

    public DataEntry dataByKey(String key) {
        return node().dataByKey(address(), key);
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
        Issue issue = new Issue(this);
        i.accept(issue);

        return node().send(issue);
    }

    public IssueTransaction issuesNft(Consumer<IssueNft> i) {
        IssueNft nft = new IssueNft(this);
        i.accept(nft);
        return issues(a -> {
            a.name(nft.name).description(nft.description).quantity(nft.quantity)
                    .decimals(nft.decimals).reissuable(nft.isReissuable).fee(nft.calcFee());
            a.compiledScript = nft.compiledScript;
        });
    }

    public TransferTransaction transfers(Consumer<Transfer> t) {
        Transfer tr = new Transfer(this);
        t.accept(tr);

        return node().send(tr);
    }

    public ReissueTransaction reissues(Consumer<Reissue> r) {
        Reissue ri = new Reissue(this);
        r.accept(ri);

        return node().send(ri);
    }

    public BurnTransaction burns(Consumer<Burn> b) {
        Burn bu = new Burn(this);
        b.accept(bu);

        return node().send(bu);
    }

    public ExchangeTransaction exchanges(Consumer<Exchange> e) {
        Exchange ex = new Exchange(this);
        e.accept(ex);

        return node().send(ex);
    }

    public LeaseTransaction leases(Consumer<Lease> lease) {
        Lease l = new Lease(this);
        lease.accept(l);

        return node().send(l);
    }

    public LeaseCancelTransaction cancelsLease(Consumer<LeaseCancel> l) {
        LeaseCancel lc = new LeaseCancel(this);
        l.accept(lc);

        return node().send(lc);
    }

    public AliasTransaction createsAlias(Consumer<CreateAlias> a) {
        CreateAlias ca = new CreateAlias(this);
        a.accept(ca);

        return node().send(ca);
    }

    public MassTransferTransaction massTransfers(Consumer<MassTransfer> m) {
        MassTransfer mt = new MassTransfer(this);
        m.accept(mt);

        return node().send(mt);
    }

    public DataTransaction writes(Consumer<WriteData> d) {
        WriteData wd = new WriteData(this);
        d.accept(wd);

        return node().send(wd);
    }

    public SetScriptTransaction setsScript(Consumer<SetScript> s) {
        SetScript ss = new SetScript(this);
        s.accept(ss);

        return node().send(ss);
    }

    public SponsorTransaction sponsors(Consumer<SponsorFee> s) {
        SponsorFee sf = new SponsorFee(this);
        s.accept(sf);

        return node().send(sf);
    }

    public SetAssetScriptTransaction setsAssetScript(Consumer<SetAssetScript> s) {
        SetAssetScript sa = new SetAssetScript(this);
        s.accept(sa);

        return node().send(sa);
    }

    public InvokeScriptTransaction invokes(Consumer<InvokeScript> i) {
        InvokeScript is = new InvokeScript(this);
        i.accept(is);

        return node().send(is);
    }
}
