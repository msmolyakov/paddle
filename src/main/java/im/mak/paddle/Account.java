package im.mak.paddle;

import com.wavesplatform.wavesj.*;
import im.mak.paddle.actions.*;
import im.mak.paddle.api.TxInfo;
import im.mak.waves.crypto.Crypto;
import im.mak.waves.transactions.*;
import im.mak.waves.transactions.account.Address;
import im.mak.waves.transactions.account.PrivateKey;
import im.mak.waves.transactions.account.PublicKey;
import im.mak.waves.transactions.common.*;
import im.mak.waves.transactions.data.*;

import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import static im.mak.paddle.Node.node;

public class Account {

    private final PrivateKey privateKey;
    private final Base58String seedBytes;
    private final String seedPhrase;

    public Account(PrivateKey privateKey, long initialWavesBalance) {
        this.privateKey = Common.notNull(privateKey, "Private key");
        this.seedBytes = Base58String.empty();
        this.seedPhrase = null;

        if (initialWavesBalance > 0)
            node().faucet().transfer(t -> t.amount(initialWavesBalance).to(this));
    }

    public Account(PrivateKey privateKey) {
        this(privateKey, 0);
    }

    public Account(String seedPhrase, long initialWavesBalance) {
        this(PrivateKey.fromSeed(seedPhrase), initialWavesBalance);
    }

    public Account(String seedPhrase) {
        this(PrivateKey.fromSeed(seedPhrase), 0);
    }

    public Account(byte[] seedBytes, long initialWavesBalance) {
        this(PrivateKey.fromSeed(seedBytes), initialWavesBalance);
    }

    public Account(byte[] seedBytes) {
        this(PrivateKey.fromSeed(seedBytes), 0);
    }

    public Account(long initialWavesBalance) {
        this(Crypto.getRandomSeedPhrase(), initialWavesBalance);
    }

    public Account() {
        this(0);
    }

    public String seedPhrase() {
        return this.seedPhrase;
    }

    public Base58String seedBytes() {
        return this.seedBytes;
    }

    public PrivateKey privateKey() {
        return this.privateKey;
    }

    public PublicKey publicKey() {
        return this.privateKey.publicKey();
    }

    public Address address() {
        return this.publicKey().address();
    }

    public ScriptInfo getScriptInfo() {
        return node().getScriptInfo(address());
    }

    public ScriptMeta getScriptMeta() {
        return node().getScriptMeta(address());
    }

    public long getWavesBalance() {
        return node().getBalance(address());
    }

    public BalanceDetails getWavesBalanceDetails() {
        return node().getBalanceDetails(address());
    }

    public List<HistoryBalance> getWavesBalanceHistory() {
        return node().getBalanceHistory(address());
    }

    public List<LeaseTransaction> getActiveLeases() {
        return node().getActiveLeases(address());
    }

    public List<AssetBalance> getAssetsBalance() {
        return node().getAssetsBalance(address());
    }

    public long getAssetBalance(AssetId assetId) {
        return node().getAssetBalance(address(), assetId);
    }

    public List<AssetDetails> getNft(int limit, AssetId after) {
        return node().getNft(address(), limit, after);
    }

    public List<AssetDetails> getNft(int limit) {
        return node().getNft(address(), limit);
    }

    public List<AssetDetails> getNft() {
        return node().getNft(address());
    }

    public List<Alias> getAliases() {
        return node().getAliasesByAddress(address());
    }

    public List<Block> getGeneratedBlocks(int fromHeight, int toHeight) {
        return node().getBlocksGeneratedBy(address(), fromHeight, toHeight);
    }

    public List<DataEntry> getData() {
        return node().getData(address());
    }

    public List<DataEntry> getData(List<String> keys) {
        return node().getData(address(), keys);
    }

    public DataEntry getData(String key) {
        return node().getData(address(), key);
    }

    public List<DataEntry> getData(Pattern regex) {
        return node().getData(address(), regex);
    }

    public Base64String getBinaryData(String key) {
        return ((BinaryEntry) getData(key)).value();
    }

    public boolean getBooleanData(String key) {
        return ((BooleanEntry) getData(key)).value();
    }

    public long getIntegerData(String key) {
        return ((IntegerEntry) getData(key)).value();
    }

    public String getStringData(String key) {
        return ((StringEntry) getData(key)).value();
    }

    public List<TransactionDebugInfo> getTransactionsHistory() {
        return node().getStateChangesByAddress(address());
    }

    public List<TransactionDebugInfo> getTransactionsHistory(int limit) {
        return node().getStateChangesByAddress(address(), limit);
    }

    public List<TransactionDebugInfo> getTransactionsHistory(int limit, Id afterTxId) {
        return node().getStateChangesByAddress(address(), limit, afterTxId);
    }

    public Proof sign(byte[] bytes) {
        return Proof.as(privateKey.sign(bytes));
    }

    public TxInfo<IssueTransaction> issue(Consumer<Issue> i) {
        Issue issue = new Issue(this);
        i.accept(issue);
        IssueTransaction tx = IssueTransaction
                .builder(issue.name, issue.quantity, issue.decimals)
                .description(issue.description)
                .isReissuable(issue.reissuable)
                .script(issue.compiledScript)
                .fee(issue.calcFee())
                //TODO other optional fields (timestamp, ...) here and in the same methods
                .getSignedWith(issue.sender.privateKey());

        return node().waitForTransaction(node().broadcast(tx).id(), IssueTransaction.class);
    }

    public TxInfo<IssueTransaction> issueNft(Consumer<IssueNft> i) {
        IssueNft nft = new IssueNft(this);
        i.accept(nft);
        IssueTransaction tx = IssueTransaction
                .builderNFT(nft.name)
                .description(nft.description)
                .script(nft.compiledScript)
                .fee(nft.calcFee())
                .getSignedWith(nft.sender.privateKey());

        return node().waitForTransaction(node().broadcast(tx).id(), IssueTransaction.class);
    }

    public TxInfo<TransferTransaction> transfer(Consumer<Transfer> t) {
        Transfer tr = new Transfer(this);
        t.accept(tr);
        TransferTransaction tx = TransferTransaction
                .builder(tr.recipient, tr.amount)
                .attachment(tr.attachment)
                .fee(Amount.of(tr.calcFee(), tr.feeAssetId))
                .getSignedWith(tr.sender.privateKey());

        return node().waitForTransaction(node().broadcast(tx).id(), TransferTransaction.class);
    }

    public TxInfo<ReissueTransaction> reissue(Consumer<Reissue> r) {
        Reissue ri = new Reissue(this);
        r.accept(ri);
        ReissueTransaction tx = ReissueTransaction
                .builder(Amount.of(ri.amount, ri.assetId))
                .reissuable(ri.reissuable)
                .fee(ri.calcFee())
                .getSignedWith(ri.sender.privateKey());

        return node().waitForTransaction(node().broadcast(tx).id(), ReissueTransaction.class);
    }

    public TxInfo<BurnTransaction> burn(Consumer<Burn> b) {
        Burn bu = new Burn(this);
        b.accept(bu);
        BurnTransaction tx = BurnTransaction
                .builder(Amount.of(bu.amount, bu.assetId))
                .fee(bu.calcFee())
                .getSignedWith(bu.sender.privateKey());

        return node().waitForTransaction(node().broadcast(tx).id(), BurnTransaction.class);
    }

    public TxInfo<ExchangeTransaction> exchange(Consumer<Exchange> e) {
        Exchange ex = new Exchange(this);
        e.accept(ex);
        ExchangeTransaction tx = ExchangeTransaction
                .builder(ex.order1, ex.order2, ex.amount, ex.price, ex.buyMatcherFee, ex.sellMatcherFee)
                .fee(ex.calcFee())
                .getSignedWith(ex.sender.privateKey());

        return node().waitForTransaction(node().broadcast(tx).id(), ExchangeTransaction.class);
    }

    public TxInfo<LeaseTransaction> lease(Consumer<Lease> lease) {
        Lease l = new Lease(this);
        lease.accept(l);
        LeaseTransaction tx = LeaseTransaction
                .builder(l.recipient, l.amount)
                .fee(l.calcFee())
                .getSignedWith(l.sender.privateKey());

        return node().waitForTransaction(node().broadcast(tx).id(), LeaseTransaction.class);
    }

    public TxInfo<LeaseCancelTransaction> cancelLease(Consumer<LeaseCancel> l) {
        LeaseCancel lc = new LeaseCancel(this);
        l.accept(lc);
        LeaseCancelTransaction tx = LeaseCancelTransaction
                .builder(lc.leaseId)
                .fee(lc.calcFee())
                .getSignedWith(lc.sender.privateKey());

        return node().waitForTransaction(node().broadcast(tx).id(), LeaseCancelTransaction.class);
    }

    public TxInfo<CreateAliasTransaction> createAlias(Consumer<CreateAlias> a) {
        CreateAlias ca = new CreateAlias(this);
        a.accept(ca);
        CreateAliasTransaction tx = CreateAliasTransaction
                .builder(ca.alias.toString())
                .fee(ca.calcFee())
                .getSignedWith(ca.sender.privateKey());

        return node().waitForTransaction(node().broadcast(tx).id(), CreateAliasTransaction.class);
    }

    public TxInfo<MassTransferTransaction> massTransfer(Consumer<MassTransfer> m) {
        MassTransfer mt = new MassTransfer(this);
        m.accept(mt);
        MassTransferTransaction tx = MassTransferTransaction
                .builder(mt.transfers)
                .assetId(mt.assetId)
                .attachment(mt.attachment)
                .fee(mt.calcFee())
                .getSignedWith(mt.sender.privateKey());

        return node().waitForTransaction(node().broadcast(tx).id(), MassTransferTransaction.class);
    }

    public TxInfo<DataTransaction> writeData(Consumer<WriteData> d) {
        WriteData wd = new WriteData(this);
        d.accept(wd);
        DataTransaction tx = DataTransaction
                .builder(wd.data)
                .fee(wd.calcFee())
                .getSignedWith(wd.sender.privateKey());

        return node().waitForTransaction(node().broadcast(tx).id(), DataTransaction.class);
    }

    public TxInfo<SetScriptTransaction> setScript(Consumer<SetScript> s) {
        SetScript ss = new SetScript(this);
        s.accept(ss);
        SetScriptTransaction tx = SetScriptTransaction
                .builder(ss.compiledScript)
                .fee(ss.calcFee())
                .getSignedWith(ss.sender.privateKey());

        return node().waitForTransaction(node().broadcast(tx).id(), SetScriptTransaction.class);
    }

    public TxInfo<SponsorFeeTransaction> sponsorFee(Consumer<SponsorFee> s) {
        SponsorFee sf = new SponsorFee(this);
        s.accept(sf);
        SponsorFeeTransaction tx = SponsorFeeTransaction
                .builder(sf.assetId, sf.minSponsoredAssetFee)
                .fee(sf.calcFee())
                .getSignedWith(sf.sender.privateKey());

        return node().waitForTransaction(node().broadcast(tx).id(), SponsorFeeTransaction.class);
    }

    public TxInfo<SetAssetScriptTransaction> setAssetScript(Consumer<SetAssetScript> s) {
        SetAssetScript sa = new SetAssetScript(this);
        s.accept(sa);
        SetAssetScriptTransaction tx = SetAssetScriptTransaction
                .builder(sa.assetId, sa.compiledScript)
                .fee(sa.calcFee())
                .getSignedWith(sa.sender.privateKey());

        return node().waitForTransaction(node().broadcast(tx).id(), SetAssetScriptTransaction.class);
    }

    public TxInfo<InvokeScriptTransaction> invoke(Consumer<InvokeScript> i) {
        InvokeScript is = new InvokeScript(this);
        i.accept(is);
        InvokeScriptTransaction tx = InvokeScriptTransaction
                .builder(is.dApp, is.call)
                .payments(is.payments)
                .fee(is.calcFee())
                .getSignedWith(is.sender.privateKey());

        return node().waitForTransaction(node().broadcast(tx).id(), InvokeScriptTransaction.class);
    }
}
