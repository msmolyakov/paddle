package im.mak.paddle;

import com.wavesplatform.wavesj.*;
import im.mak.paddle.params.*;
import im.mak.paddle.api.TxInfo;
import com.wavesplatform.crypto.Crypto;
import com.wavesplatform.transactions.*;
import com.wavesplatform.transactions.account.Address;
import com.wavesplatform.transactions.account.PrivateKey;
import com.wavesplatform.transactions.account.PublicKey;
import com.wavesplatform.transactions.common.*;
import com.wavesplatform.transactions.data.*;

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

    public TxInfo<IssueTransaction> issue(Consumer<IssueParams> i) {
        IssueParams issue = new IssueParams(this);
        i.accept(issue);
        IssueTransaction tx = IssueTransaction
                .builder(issue.getName(), issue.getQuantity(), issue.getDecimals())
                .description(issue.getDescription())
                .isReissuable(issue.isReissuable())
                .script(issue.getCompiledScript())
                .fee(issue.getFee())
                .timestamp(issue.getTimestamp() == 0 ? System.currentTimeMillis() : issue.getTimestamp())
                .getSignedWith(issue.getSender().privateKey());

        return node().waitForTransaction(node().broadcast(tx).id(), IssueTransaction.class);
    }

    public TxInfo<IssueTransaction> issueNft(Consumer<IssueNftParams> i) {
        IssueNftParams nft = new IssueNftParams(this);
        i.accept(nft);
        IssueTransaction tx = IssueTransaction
                .builderNFT(nft.getName())
                .description(nft.getDescription())
                .script(nft.getCompiledScript())
                .fee(nft.getFee())
                .timestamp(nft.getTimestamp() == 0 ? System.currentTimeMillis() : nft.getTimestamp())
                .getSignedWith(nft.getSender().privateKey());

        return node().waitForTransaction(node().broadcast(tx).id(), IssueTransaction.class);
    }

    public TxInfo<TransferTransaction> transfer(Consumer<TransferParams> t) {
        TransferParams tr = new TransferParams(this);
        t.accept(tr);
        TransferTransaction tx = TransferTransaction
                .builder(tr.getRecipient(), tr.getAmount())
                .attachment(tr.getAttachment())
                .fee(Amount.of(tr.getFee(), tr.getFeeAssetId()))
                .timestamp(tr.getTimestamp() == 0 ? System.currentTimeMillis() : tr.getTimestamp())
                .getSignedWith(tr.getSender().privateKey());

        return node().waitForTransaction(node().broadcast(tx).id(), TransferTransaction.class);
    }

    public TxInfo<ReissueTransaction> reissue(Consumer<ReissueParams> r) {
        ReissueParams ri = new ReissueParams(this);
        r.accept(ri);
        ReissueTransaction tx = ReissueTransaction
                .builder(ri.getAmount())
                .reissuable(ri.isReissuable())
                .fee(ri.getFee())
                .timestamp(ri.getTimestamp() == 0 ? System.currentTimeMillis() : ri.getTimestamp())
                .getSignedWith(ri.getSender().privateKey());

        return node().waitForTransaction(node().broadcast(tx).id(), ReissueTransaction.class);
    }

    public TxInfo<BurnTransaction> burn(Consumer<BurnParams> b) {
        BurnParams bu = new BurnParams(this);
        b.accept(bu);
        BurnTransaction tx = BurnTransaction
                .builder(bu.getAmount())
                .fee(bu.getFee())
                .timestamp(bu.getTimestamp() == 0 ? System.currentTimeMillis() : bu.getTimestamp())
                .getSignedWith(bu.getSender().privateKey());

        return node().waitForTransaction(node().broadcast(tx).id(), BurnTransaction.class);
    }

    public TxInfo<ExchangeTransaction> exchange(Consumer<ExchangeParams> e) {
        ExchangeParams ex = new ExchangeParams(this);
        e.accept(ex);
        ExchangeTransaction tx = ExchangeTransaction
                .builder(ex.getOrder1(), ex.getOrder2(), ex.getAmount(), ex.getPrice(),
                        ex.getBuyMatcherFee(), ex.getSellMatcherFee())
                .fee(ex.getFee())
                .timestamp(ex.getTimestamp() == 0 ? System.currentTimeMillis() : ex.getTimestamp())
                .getSignedWith(ex.getSender().privateKey());

        return node().waitForTransaction(node().broadcast(tx).id(), ExchangeTransaction.class);
    }

    public TxInfo<LeaseTransaction> lease(Consumer<LeaseParams> lease) {
        LeaseParams l = new LeaseParams(this);
        lease.accept(l);
        LeaseTransaction tx = LeaseTransaction
                .builder(l.getRecipient(), l.getAmount())
                .fee(l.getFee())
                .timestamp(l.getTimestamp() == 0 ? System.currentTimeMillis() : l.getTimestamp())
                .getSignedWith(l.getSender().privateKey());

        return node().waitForTransaction(node().broadcast(tx).id(), LeaseTransaction.class);
    }

    public TxInfo<LeaseCancelTransaction> cancelLease(Consumer<LeaseCancelParams> l) {
        LeaseCancelParams lc = new LeaseCancelParams(this);
        l.accept(lc);
        LeaseCancelTransaction tx = LeaseCancelTransaction
                .builder(lc.getLeaseId())
                .fee(lc.getFee())
                .timestamp(lc.getTimestamp() == 0 ? System.currentTimeMillis() : lc.getTimestamp())
                .getSignedWith(lc.getSender().privateKey());

        return node().waitForTransaction(node().broadcast(tx).id(), LeaseCancelTransaction.class);
    }

    public TxInfo<CreateAliasTransaction> createAlias(Consumer<CreateAliasParams> a) {
        CreateAliasParams ca = new CreateAliasParams(this);
        a.accept(ca);
        CreateAliasTransaction tx = CreateAliasTransaction
                .builder(ca.getAlias().toString())
                .fee(ca.getFee())
                .timestamp(ca.getTimestamp() == 0 ? System.currentTimeMillis() : ca.getTimestamp())
                .getSignedWith(ca.getSender().privateKey());

        return node().waitForTransaction(node().broadcast(tx).id(), CreateAliasTransaction.class);
    }

    public TxInfo<MassTransferTransaction> massTransfer(Consumer<MassTransferParams> m) {
        MassTransferParams mt = new MassTransferParams(this);
        m.accept(mt);
        MassTransferTransaction tx = MassTransferTransaction
                .builder(mt.getTransfers())
                .assetId(mt.getAssetId())
                .attachment(mt.getAttachment())
                .fee(mt.getFee())
                .timestamp(mt.getTimestamp() == 0 ? System.currentTimeMillis() : mt.getTimestamp())
                .getSignedWith(mt.getSender().privateKey());

        return node().waitForTransaction(node().broadcast(tx).id(), MassTransferTransaction.class);
    }

    public TxInfo<DataTransaction> writeData(Consumer<DataParams> d) {
        DataParams wd = new DataParams(this);
        d.accept(wd);
        DataTransaction tx = DataTransaction
                .builder(wd.getData())
                .fee(wd.getFee())
                .timestamp(wd.getTimestamp() == 0 ? System.currentTimeMillis() : wd.getTimestamp())
                .getSignedWith(wd.getSender().privateKey());

        return node().waitForTransaction(node().broadcast(tx).id(), DataTransaction.class);
    }

    public TxInfo<SetScriptTransaction> setScript(Consumer<SetScriptParams> s) {
        SetScriptParams ss = new SetScriptParams(this);
        s.accept(ss);
        SetScriptTransaction tx = SetScriptTransaction
                .builder(ss.getCompiledScript())
                .fee(ss.getFee())
                .timestamp(ss.getTimestamp() == 0 ? System.currentTimeMillis() : ss.getTimestamp())
                .getSignedWith(ss.getSender().privateKey());

        return node().waitForTransaction(node().broadcast(tx).id(), SetScriptTransaction.class);
    }

    public TxInfo<SponsorFeeTransaction> sponsorFee(Consumer<SponsorFeeParams> s) {
        SponsorFeeParams sf = new SponsorFeeParams(this);
        s.accept(sf);
        SponsorFeeTransaction tx = SponsorFeeTransaction
                .builder(sf.getAssetId(), sf.getMinSponsoredFee())
                .fee(sf.getFee())
                .timestamp(sf.getTimestamp() == 0 ? System.currentTimeMillis() : sf.getTimestamp())
                .getSignedWith(sf.getSender().privateKey());

        return node().waitForTransaction(node().broadcast(tx).id(), SponsorFeeTransaction.class);
    }

    public TxInfo<SetAssetScriptTransaction> setAssetScript(Consumer<SetAssetScriptParams> s) {
        SetAssetScriptParams sa = new SetAssetScriptParams(this);
        s.accept(sa);
        SetAssetScriptTransaction tx = SetAssetScriptTransaction
                .builder(sa.getAssetId(), sa.getCompiledScript())
                .fee(sa.getFee())
                .timestamp(sa.getTimestamp() == 0 ? System.currentTimeMillis() : sa.getTimestamp())
                .getSignedWith(sa.getSender().privateKey());

        return node().waitForTransaction(node().broadcast(tx).id(), SetAssetScriptTransaction.class);
    }

    public TxInfo<InvokeScriptTransaction> invoke(Consumer<InvokeScriptParams> i) {
        InvokeScriptParams is = new InvokeScriptParams(this);
        i.accept(is);
        InvokeScriptTransaction tx = InvokeScriptTransaction
                .builder(is.getDApp(), is.getCall())
                .payments(is.getPayments())
                .fee(is.getFee())
                .timestamp(is.getTimestamp() == 0 ? System.currentTimeMillis() : is.getTimestamp())
                .getSignedWith(is.getSender().privateKey());

        return node().waitForTransaction(node().broadcast(tx).id(), InvokeScriptTransaction.class);
    }

    public TxInfo<UpdateAssetInfoTransaction> updateAssetInfo(Consumer<UpdateAssetInfoParams> u) {
        UpdateAssetInfoParams uai = new UpdateAssetInfoParams(this);
        u.accept(uai);
        UpdateAssetInfoTransaction tx = UpdateAssetInfoTransaction
                .builder(uai.getAssetId(), uai.getName(), uai.getDescription())
                .fee(uai.getFee())
                .timestamp(uai.getTimestamp() == 0 ? System.currentTimeMillis() : uai.getTimestamp())
                .getSignedWith(uai.getSender().privateKey());

        return node().waitForTransaction(node().broadcast(tx).id(), UpdateAssetInfoTransaction.class);
    }
}
