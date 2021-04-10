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

    private static <B extends Transaction.TransactionBuilder<?, ?>, P extends TxParams<?>>
    Transaction setCommonFieldsAndGetSigned(P params, B builder) {
        return signAndGet(params.getSignersAndProofs(),
                builder.sender(params.getSender().publicKey())
                        .fee(Amount.of(params.getFee(), params.getFeeAssetId()))
                        .timestamp(params.getTimestamp() == 0 ? System.currentTimeMillis() : params.getTimestamp())
                        .getUnsigned());
    }

    /* TRANSACTIONS */

    public TxInfo<IssueTransaction> issue(Consumer<IssueParams> is) {
        IssueParams issue = new IssueParams(this);
        is.accept(issue);
        Transaction signedTx = setCommonFieldsAndGetSigned(issue,
                IssueTransaction.builder(issue.getName(), issue.getQuantity(), issue.getDecimals())
                        .description(issue.getDescription())
                        .isReissuable(issue.isReissuable())
                        .script(issue.getCompiledScript()));
        return node().waitForTransaction(node().broadcast(signedTx).id(), IssueTransaction.class);
    }

    public TxInfo<IssueTransaction> issueNft(Consumer<IssueNftParams> is) {
        IssueNftParams nft = new IssueNftParams(this);
        is.accept(nft);
        Transaction signedTx = setCommonFieldsAndGetSigned(nft,
                IssueTransaction.builderNFT(nft.getName())
                        .description(nft.getDescription())
                        .script(nft.getCompiledScript()));
        return node().waitForTransaction(node().broadcast(signedTx).id(), IssueTransaction.class);
    }

    public TxInfo<TransferTransaction> transfer(Consumer<TransferParams> t) {
        TransferParams tr = new TransferParams(this);
        t.accept(tr);
        Transaction signedTx = setCommonFieldsAndGetSigned(tr,
                TransferTransaction.builder(tr.getRecipient(), tr.getAmount())
                        .attachment(tr.getAttachment()));
        return node().waitForTransaction(node().broadcast(signedTx).id(), TransferTransaction.class);
    }

    public TxInfo<ReissueTransaction> reissue(Consumer<ReissueParams> r) {
        ReissueParams ri = new ReissueParams(this);
        r.accept(ri);
        Transaction signedTx = setCommonFieldsAndGetSigned(ri,
                ReissueTransaction.builder(ri.getAmount())
                        .reissuable(ri.isReissuable()));
        return node().waitForTransaction(node().broadcast(signedTx).id(), ReissueTransaction.class);
    }

    public TxInfo<BurnTransaction> burn(Consumer<BurnParams> b) {
        BurnParams bu = new BurnParams(this);
        b.accept(bu);
        Transaction signedTx = setCommonFieldsAndGetSigned(bu, BurnTransaction.builder(bu.getAmount()));
        return node().waitForTransaction(node().broadcast(signedTx).id(), BurnTransaction.class);
    }

    public TxInfo<ExchangeTransaction> exchange(Consumer<ExchangeParams> e) {
        ExchangeParams ex = new ExchangeParams(this);
        e.accept(ex);
        Transaction signedTx = setCommonFieldsAndGetSigned(ex,
                ExchangeTransaction.builder(ex.getOrder1(), ex.getOrder2(), ex.getAmount(), ex.getPrice(),
                        ex.getBuyMatcherFee(), ex.getSellMatcherFee()));
        return node().waitForTransaction(node().broadcast(signedTx).id(), ExchangeTransaction.class);
    }

    public TxInfo<LeaseTransaction> lease(Consumer<LeaseParams> lease) {
        LeaseParams l = new LeaseParams(this);
        lease.accept(l);
        Transaction signedTx = setCommonFieldsAndGetSigned(l,
                LeaseTransaction.builder(l.getRecipient(), l.getAmount()));
        return node().waitForTransaction(node().broadcast(signedTx).id(), LeaseTransaction.class);
    }

    public TxInfo<LeaseCancelTransaction> cancelLease(Consumer<LeaseCancelParams> l) {
        LeaseCancelParams lc = new LeaseCancelParams(this);
        l.accept(lc);
        Transaction signedTx = setCommonFieldsAndGetSigned(lc, LeaseCancelTransaction.builder(lc.getLeaseId()));
        return node().waitForTransaction(node().broadcast(signedTx).id(), LeaseCancelTransaction.class);
    }

    public TxInfo<CreateAliasTransaction> createAlias(Consumer<CreateAliasParams> a) {
        CreateAliasParams ca = new CreateAliasParams(this);
        a.accept(ca);
        Transaction signedTx = setCommonFieldsAndGetSigned(ca, CreateAliasTransaction.builder(ca.getAlias().toString()));
        return node().waitForTransaction(node().broadcast(signedTx).id(), CreateAliasTransaction.class);
    }

    public TxInfo<MassTransferTransaction> massTransfer(Consumer<MassTransferParams> m) {
        MassTransferParams mt = new MassTransferParams(this);
        m.accept(mt);
        Transaction signedTx = setCommonFieldsAndGetSigned(mt,
                MassTransferTransaction.builder(mt.getTransfers())
                        .assetId(mt.getAssetId())
                        .attachment(mt.getAttachment()));
        return node().waitForTransaction(node().broadcast(signedTx).id(), MassTransferTransaction.class);
    }

    public TxInfo<DataTransaction> writeData(Consumer<DataParams> d) {
        DataParams wd = new DataParams(this);
        d.accept(wd);
        Transaction signedTx = setCommonFieldsAndGetSigned(wd, DataTransaction.builder(wd.getData()));
        return node().waitForTransaction(node().broadcast(signedTx).id(), DataTransaction.class);
    }

    public TxInfo<SetScriptTransaction> setScript(Consumer<SetScriptParams> s) {
        SetScriptParams ss = new SetScriptParams(this);
        s.accept(ss);
        Transaction signedTx = setCommonFieldsAndGetSigned(ss, SetScriptTransaction.builder(ss.getCompiledScript()));
        return node().waitForTransaction(node().broadcast(signedTx).id(), SetScriptTransaction.class);
    }

    public TxInfo<SponsorFeeTransaction> sponsorFee(Consumer<SponsorFeeParams> s) {
        SponsorFeeParams sf = new SponsorFeeParams(this);
        s.accept(sf);
        Transaction signedTx = setCommonFieldsAndGetSigned(sf,
                SponsorFeeTransaction.builder(sf.getAssetId(), sf.getMinSponsoredFee()));
        return node().waitForTransaction(node().broadcast(signedTx).id(), SponsorFeeTransaction.class);
    }

    public TxInfo<SetAssetScriptTransaction> setAssetScript(Consumer<SetAssetScriptParams> s) {
        SetAssetScriptParams sa = new SetAssetScriptParams(this);
        s.accept(sa);
        Transaction signedTx = setCommonFieldsAndGetSigned(sa,
                SetAssetScriptTransaction.builder(sa.getAssetId(), sa.getCompiledScript()));
        return node().waitForTransaction(node().broadcast(signedTx).id(), SetAssetScriptTransaction.class);
    }

    public TxInfo<InvokeScriptTransaction> invoke(Consumer<InvokeScriptParams> i) {
        InvokeScriptParams is = new InvokeScriptParams(this);
        i.accept(is);
        Transaction signedTx = setCommonFieldsAndGetSigned(is,
                InvokeScriptTransaction.builder(is.getDApp(), is.getCall()).payments(is.getPayments()));
        return node().waitForTransaction(node().broadcast(signedTx).id(), InvokeScriptTransaction.class);
    }

    public TxInfo<InvokeScriptTransaction> invoke(DAppCall call, Amount... payments) {
        return invoke(i -> i.dApp(call.getDApp()).function(call.getFunction()).payments(payments));
    }

    public TxInfo<InvokeScriptTransaction> invoke(DAppCall call, Consumer<InvokeScriptParams> i) {
        return invoke(i.andThen(is -> is.dApp(call.getDApp()).function(call.getFunction())));
    }

    public TxInfo<UpdateAssetInfoTransaction> updateAssetInfo(Consumer<UpdateAssetInfoParams> u) {
        UpdateAssetInfoParams uai = new UpdateAssetInfoParams(this);
        u.accept(uai);
        Transaction signedTx = setCommonFieldsAndGetSigned(uai,
                UpdateAssetInfoTransaction.builder(uai.getAssetId(), uai.getName(), uai.getDescription()));
        return node().waitForTransaction(node().broadcast(signedTx).id(), UpdateAssetInfoTransaction.class);
    }

}
