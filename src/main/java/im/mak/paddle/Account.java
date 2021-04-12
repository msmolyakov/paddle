package im.mak.paddle;

import com.wavesplatform.wavesj.*;
import im.mak.paddle.invocation.DAppCall;
import im.mak.paddle.params.*;
import im.mak.paddle.api.TxInfo;
import com.wavesplatform.crypto.Crypto;
import com.wavesplatform.transactions.*;
import com.wavesplatform.transactions.account.Address;
import com.wavesplatform.transactions.account.PrivateKey;
import com.wavesplatform.transactions.account.PublicKey;
import com.wavesplatform.transactions.common.*;
import com.wavesplatform.transactions.data.*;
import im.mak.paddle.params.readable.*;
import im.mak.paddle.token.Asset;
import im.mak.paddle.token.Token;
import org.apache.commons.lang.IllegalClassException;

import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import static im.mak.paddle.Node.node;

@SuppressWarnings({"UnusedReturnValue", "unused"})
public class Account {

    private final PrivateKey privateKey;
    private final Base58String seedBytes;
    private final String seedPhrase;

    /* CONSTRUCTOR */

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

    /* ACCOUNT */

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

    /* REST API */

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

    public long getAssetBalance(Asset asset) {
        return this.getAssetBalance(asset.id());
    }

    public long getBalance(Token token) {
        return token.id().isWaves() ? getWavesBalance() : getAssetBalance((AssetId) token);
    }

    public List<AssetDetails> getNft(int limit, AssetId after) {
        return node().getNft(address(), limit, after);
    }

    public List<AssetDetails> getNft(int limit, Asset after) {
        return getNft(limit, after.id());
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

    /* SIGN */

    public Proof signBytes(byte[] bytes) {
        return Proof.as(privateKey.sign(bytes));
    }

    public Account sign(TransactionOrOrder txOrOrder) {
        txOrOrder.addProof(this.privateKey());
        return this;
    }

    public <T extends TransactionOrOrder> T signAndGet(T tx) {
        return tx.addProof(this.privateKey());
    }

    private static <T extends TransactionOrOrder> T signAndGet(List<Object> signersAndProofs, T unsigned) {
        signersAndProofs.forEach(item -> {
            if (item == null)
                unsigned.addProof(Proof.EMPTY);
            else if (item instanceof PrivateKey)
                unsigned.addProof((PrivateKey) item);
            else if (item instanceof Account)
                unsigned.addProof(((Account) item).privateKey());
            else if (item instanceof Proof)
                unsigned.addProof((Proof) item);
            else
                throw new IllegalClassException("Expected: " + PrivateKey.class.getName()
                        + " or " + Account.class.getName() + " or " + Proof.class.getName()
                        + ", actual: " + item.getClass().getName());
        });
        return unsigned;
    }

    /* TRANSACTIONS */

    public TxInfo<IssueTransaction> issue(Consumer<IssueParams> is) {
        IssueParamsReadable issue = new IssueParamsReadable(this);
        is.accept(issue);
        Transaction signedTx = signAndGet(issue.getSignersAndProofs(),
                IssueTransaction.builder(issue.getName(), issue.getQuantity(), issue.getDecimals())
                        .description(issue.getDescription())
                        .isReissuable(issue.isReissuable())
                        .script(issue.getCompiledScript())
                        .sender(issue.getSender().publicKey())
                        .fee(Amount.of(issue.getFee(), issue.getFeeAssetId()))
                        .timestamp(issue.getTimestamp() == 0 ? System.currentTimeMillis() : issue.getTimestamp())
                        .getUnsigned());
        return node().waitForTransaction(node().broadcast(signedTx).id(), IssueTransaction.class);
    }

    public TxInfo<IssueTransaction> issueNft(Consumer<IssueNftParams> is) {
        IssueNftParamsReadable nft = new IssueNftParamsReadable(this);
        is.accept(nft);
        Transaction signedTx = signAndGet(nft.getSignersAndProofs(),
                IssueTransaction.builderNFT(nft.getName())
                        .description(nft.getDescription())
                        .script(nft.getCompiledScript())
                        .sender(nft.getSender().publicKey())
                        .fee(Amount.of(nft.getFee(), nft.getFeeAssetId()))
                        .timestamp(nft.getTimestamp() == 0 ? System.currentTimeMillis() : nft.getTimestamp())
                        .getUnsigned());
        return node().waitForTransaction(node().broadcast(signedTx).id(), IssueTransaction.class);
    }

    public TxInfo<IssueTransaction> issueNft() {
        return issueNft(i -> {});
    }

    public TxInfo<TransferTransaction> transfer(Consumer<TransferParams> t) {
        TransferParamsReadable tr = new TransferParamsReadable(this);
        t.accept(tr);
        Transaction signedTx = signAndGet(tr.getSignersAndProofs(),
                TransferTransaction.builder(tr.getRecipient(), tr.getAmount())
                        .attachment(tr.getAttachment())
                        .sender(tr.getSender().publicKey())
                        .fee(Amount.of(tr.getFee(), tr.getFeeAssetId()))
                        .timestamp(tr.getTimestamp() == 0 ? System.currentTimeMillis() : tr.getTimestamp())
                        .getUnsigned());
        return node().waitForTransaction(node().broadcast(signedTx).id(), TransferTransaction.class);
    }

    public TxInfo<ReissueTransaction> reissue(Consumer<ReissueParams> r) {
        ReissueParamsReadable ri = new ReissueParamsReadable(this);
        r.accept(ri);
        Transaction signedTx = signAndGet(ri.getSignersAndProofs(),
                ReissueTransaction.builder(ri.getAmount())
                        .reissuable(ri.isReissuable())
                        .sender(ri.getSender().publicKey())
                        .fee(Amount.of(ri.getFee(), ri.getFeeAssetId()))
                        .timestamp(ri.getTimestamp() == 0 ? System.currentTimeMillis() : ri.getTimestamp())
                        .getUnsigned());
        return node().waitForTransaction(node().broadcast(signedTx).id(), ReissueTransaction.class);
    }

    public TxInfo<BurnTransaction> burn(Consumer<BurnParams> b) {
        BurnParamsReadable bu = new BurnParamsReadable(this);
        b.accept(bu);
        Transaction signedTx = signAndGet(bu.getSignersAndProofs(),
                BurnTransaction.builder(bu.getAmount())
                        .sender(bu.getSender().publicKey())
                        .fee(Amount.of(bu.getFee(), bu.getFeeAssetId()))
                        .timestamp(bu.getTimestamp() == 0 ? System.currentTimeMillis() : bu.getTimestamp())
                        .getUnsigned());
        return node().waitForTransaction(node().broadcast(signedTx).id(), BurnTransaction.class);
    }

    public TxInfo<ExchangeTransaction> exchange(Consumer<ExchangeParams> e) {
        ExchangeParamsReadable ex = new ExchangeParamsReadable(this);
        e.accept(ex);
        Transaction signedTx = signAndGet(ex.getSignersAndProofs(),
                ExchangeTransaction.builder(ex.getOrder1(), ex.getOrder2(), ex.getAmount(), ex.getPrice(), ex.getBuyMatcherFee(), ex.getSellMatcherFee())
                        .sender(ex.getSender().publicKey())
                        .fee(Amount.of(ex.getFee(), ex.getFeeAssetId()))
                        .timestamp(ex.getTimestamp() == 0 ? System.currentTimeMillis() : ex.getTimestamp())
                        .getUnsigned());
        return node().waitForTransaction(node().broadcast(signedTx).id(), ExchangeTransaction.class);
    }

    public TxInfo<LeaseTransaction> lease(Consumer<LeaseParams> lease) {
        LeaseParamsReadable l = new LeaseParamsReadable(this);
        lease.accept(l);
        Transaction signedTx = signAndGet(l.getSignersAndProofs(),
                LeaseTransaction.builder(l.getRecipient(), l.getAmount())
                        .sender(l.getSender().publicKey())
                        .fee(Amount.of(l.getFee(), l.getFeeAssetId()))
                        .timestamp(l.getTimestamp() == 0 ? System.currentTimeMillis() : l.getTimestamp())
                        .getUnsigned());
        return node().waitForTransaction(node().broadcast(signedTx).id(), LeaseTransaction.class);
    }

    public TxInfo<LeaseCancelTransaction> cancelLease(Consumer<LeaseCancelParams> l) {
        LeaseCancelParamsReadable lc = new LeaseCancelParamsReadable(this);
        l.accept(lc);
        Transaction signedTx = signAndGet(lc.getSignersAndProofs(),
                LeaseCancelTransaction.builder(lc.getLeaseId())
                        .sender(lc.getSender().publicKey())
                        .fee(Amount.of(lc.getFee(), lc.getFeeAssetId()))
                        .timestamp(lc.getTimestamp() == 0 ? System.currentTimeMillis() : lc.getTimestamp())
                        .getUnsigned());
        return node().waitForTransaction(node().broadcast(signedTx).id(), LeaseCancelTransaction.class);
    }

    public TxInfo<CreateAliasTransaction> createAlias(Consumer<CreateAliasParams> a) {
        CreateAliasParamsReadable ca = new CreateAliasParamsReadable(this);
        a.accept(ca);
        Transaction signedTx = signAndGet(ca.getSignersAndProofs(),
                CreateAliasTransaction.builder(ca.getAlias().toString())
                        .sender(ca.getSender().publicKey())
                        .fee(Amount.of(ca.getFee(), ca.getFeeAssetId()))
                        .timestamp(ca.getTimestamp() == 0 ? System.currentTimeMillis() : ca.getTimestamp())
                        .getUnsigned());
        return node().waitForTransaction(node().broadcast(signedTx).id(), CreateAliasTransaction.class);
    }

    public TxInfo<MassTransferTransaction> massTransfer(Consumer<MassTransferParams> m) {
        MassTransferParamsReadable mt = new MassTransferParamsReadable(this);
        m.accept(mt);
        Transaction signedTx = signAndGet(mt.getSignersAndProofs(),
                MassTransferTransaction.builder(mt.getTransfers())
                        .assetId(mt.getAssetId())
                        .attachment(mt.getAttachment())
                        .sender(mt.getSender().publicKey())
                        .fee(Amount.of(mt.getFee(), mt.getFeeAssetId()))
                        .timestamp(mt.getTimestamp() == 0 ? System.currentTimeMillis() : mt.getTimestamp())
                        .getUnsigned());
        return node().waitForTransaction(node().broadcast(signedTx).id(), MassTransferTransaction.class);
    }

    public TxInfo<DataTransaction> writeData(Consumer<DataParams> d) {
        DataParamsReadable wd = new DataParamsReadable(this);
        d.accept(wd);
        Transaction signedTx = signAndGet(wd.getSignersAndProofs(),
                DataTransaction.builder(wd.getData())
                        .sender(wd.getSender().publicKey())
                        .fee(Amount.of(wd.getFee(), wd.getFeeAssetId()))
                        .timestamp(wd.getTimestamp() == 0 ? System.currentTimeMillis() : wd.getTimestamp())
                        .getUnsigned());
        return node().waitForTransaction(node().broadcast(signedTx).id(), DataTransaction.class);
    }

    public TxInfo<SetScriptTransaction> setScript(Consumer<SetScriptParams> s) {
        SetScriptParamsReadable ss = new SetScriptParamsReadable(this);
        s.accept(ss);
        Transaction signedTx = signAndGet(ss.getSignersAndProofs(),
                SetScriptTransaction.builder(ss.getCompiledScript())
                        .sender(ss.getSender().publicKey())
                        .fee(Amount.of(ss.getFee(), ss.getFeeAssetId()))
                        .timestamp(ss.getTimestamp() == 0 ? System.currentTimeMillis() : ss.getTimestamp())
                        .getUnsigned());
        return node().waitForTransaction(node().broadcast(signedTx).id(), SetScriptTransaction.class);
    }

    public TxInfo<SponsorFeeTransaction> sponsorFee(Consumer<SponsorFeeParams> s) {
        SponsorFeeParamsReadable sf = new SponsorFeeParamsReadable(this);
        s.accept(sf);
        Transaction signedTx = signAndGet(sf.getSignersAndProofs(),
                SponsorFeeTransaction.builder(sf.getAssetId(), sf.getMinSponsoredFee())
                        .sender(sf.getSender().publicKey())
                        .fee(Amount.of(sf.getFee(), sf.getFeeAssetId()))
                        .timestamp(sf.getTimestamp() == 0 ? System.currentTimeMillis() : sf.getTimestamp())
                        .getUnsigned());
        return node().waitForTransaction(node().broadcast(signedTx).id(), SponsorFeeTransaction.class);
    }

    public TxInfo<SetAssetScriptTransaction> setAssetScript(Consumer<SetAssetScriptParams> s) {
        SetAssetScriptParamsReadable sa = new SetAssetScriptParamsReadable(this);
        s.accept(sa);
        Transaction signedTx = signAndGet(sa.getSignersAndProofs(),
                SetAssetScriptTransaction.builder(sa.getAssetId(), sa.getCompiledScript())
                        .sender(sa.getSender().publicKey())
                        .fee(Amount.of(sa.getFee(), sa.getFeeAssetId()))
                        .timestamp(sa.getTimestamp() == 0 ? System.currentTimeMillis() : sa.getTimestamp())
                        .getUnsigned());
        return node().waitForTransaction(node().broadcast(signedTx).id(), SetAssetScriptTransaction.class);
    }

    public TxInfo<InvokeScriptTransaction> invoke(Consumer<InvokeScriptParams> i) {
        InvokeScriptParamsReadable is = new InvokeScriptParamsReadable(this);
        i.accept(is);
        Transaction signedTx = signAndGet(is.getSignersAndProofs(),
                InvokeScriptTransaction.builder(is.getDApp(), is.getCall()).payments(is.getPayments())
                        .sender(is.getSender().publicKey())
                        .fee(Amount.of(is.getFee(), is.getFeeAssetId()))
                        .timestamp(is.getTimestamp() == 0 ? System.currentTimeMillis() : is.getTimestamp())
                        .getUnsigned());
        return node().waitForTransaction(node().broadcast(signedTx).id(), InvokeScriptTransaction.class);
    }

    public TxInfo<InvokeScriptTransaction> invoke(DAppCall call, Amount... payments) {
        return invoke(i -> i.dApp(call.getDApp()).function(call.getFunction()).payments(payments));
    }

    public TxInfo<InvokeScriptTransaction> invoke(DAppCall call, Consumer<InvokeScriptParams> i) {
        return invoke(i.andThen(is -> is.dApp(call.getDApp()).function(call.getFunction())));
    }

    public TxInfo<UpdateAssetInfoTransaction> updateAssetInfo(Consumer<UpdateAssetInfoParams> u) {
        UpdateAssetInfoParamsReadable uai = new UpdateAssetInfoParamsReadable(this);
        u.accept(uai);
        Transaction signedTx = signAndGet(uai.getSignersAndProofs(),
                UpdateAssetInfoTransaction.builder(uai.getAssetId(), uai.getName(), uai.getDescription())
                        .sender(uai.getSender().publicKey())
                        .fee(Amount.of(uai.getFee(), uai.getFeeAssetId()))
                        .timestamp(uai.getTimestamp() == 0 ? System.currentTimeMillis() : uai.getTimestamp())
                        .getUnsigned());
        return node().waitForTransaction(node().broadcast(signedTx).id(), UpdateAssetInfoTransaction.class);
    }

}
