package im.mak.paddle;

import com.wavesplatform.transactions.exchange.Order;
import com.wavesplatform.wavesj.*;
import com.wavesplatform.wavesj.info.*;
import im.mak.paddle.dapp.DAppCall;
import com.wavesplatform.crypto.Crypto;
import com.wavesplatform.transactions.*;
import com.wavesplatform.transactions.account.Address;
import com.wavesplatform.transactions.account.PrivateKey;
import com.wavesplatform.transactions.account.PublicKey;
import com.wavesplatform.transactions.common.*;
import com.wavesplatform.transactions.data.*;
import im.mak.paddle.token.Asset;
import im.mak.paddle.token.Token;
import im.mak.paddle.util.Script;

import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import static im.mak.paddle.Node.node;

@SuppressWarnings({"UnusedReturnValue", "unused"})
public class Account {

    private final PrivateKey privateKey;
    private final Node node;

    /* CONSTRUCTOR */
    
    public Account(PrivateKey privateKey, long initialWavesBalance, Node node) {
        this.privateKey = Common.notNull(privateKey, "Private key");
        this.node = node == null ? node() : node;

        if (initialWavesBalance > 0)
            this.node.faucet().transfer(this, initialWavesBalance, AssetId.WAVES);
    }

    public Account(PrivateKey privateKey, long initialWavesBalance) {
        this(privateKey, initialWavesBalance, null);
    }

    public Account(PrivateKey privateKey, Node node) {
        this(privateKey, 0, node);
    }

    public Account(PrivateKey privateKey) {
        this(privateKey, 0);
    }

    public Account(String seedPhrase, long initialWavesBalance, Node node) {
        this(PrivateKey.fromSeed(seedPhrase), initialWavesBalance, node);
    }

    public Account(String seedPhrase, long initialWavesBalance) {
        this(PrivateKey.fromSeed(seedPhrase), initialWavesBalance);
    }

    public Account(long initialWavesBalance, Node node) {
        this(Crypto.getRandomSeedPhrase(), initialWavesBalance, node);
    }

    public Account(long initialWavesBalance) {
        this(Crypto.getRandomSeedPhrase(), initialWavesBalance);
    }

    public Account(Node node) {
        this(0, node);
    }

    public Account() {
        this(0);
    }

    /* ACCOUNT */

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
        return node.getScriptInfo(address());
    }

    public ScriptMeta getScriptMeta() {
        return node.getScriptMeta(address());
    }

    public long getWavesBalance() {
        return node.getBalance(address());
    }

    public BalanceDetails getWavesBalanceDetails() {
        return node.getBalanceDetails(address());
    }

    public List<HistoryBalance> getWavesBalanceHistory() {
        return node.getBalanceHistory(address());
    }

    public List<LeaseInfo> getActiveLeases() {
        return node.getActiveLeases(address());
    }

    public List<AssetBalance> getAssetsBalance() {
        return node.getAssetsBalance(address());
    }

    public long getAssetBalance(AssetId assetId) {
        return node.getAssetBalance(address(), assetId);
    }

    public long getAssetBalance(Asset asset) {
        return this.getAssetBalance(asset.id());
    }

    public long getBalance(Token token) {
        return token.id().isWaves() ? getWavesBalance() : getAssetBalance((AssetId) token);
    }

    public List<AssetDetails> getNft(int limit, AssetId after) {
        return node.getNft(address(), limit, after);
    }

    public List<AssetDetails> getNft(int limit, Asset after) {
        return getNft(limit, after.id());
    }

    public List<AssetDetails> getNft(int limit) {
        return node.getNft(address(), limit);
    }

    public List<AssetDetails> getNft() {
        return node.getNft(address());
    }

    public List<Alias> getAliases() {
        return node.getAliasesByAddress(address());
    }

    public List<Block> getGeneratedBlocks(int fromHeight, int toHeight) {
        return node.getBlocksGeneratedBy(address(), fromHeight, toHeight);
    }

    public List<DataEntry> getData() {
        return node.getData(address());
    }

    public List<DataEntry> getData(List<String> keys) {
        return node.getData(address(), keys);
    }

    public DataEntry getData(String key) {
        return node.getData(address(), key);
    }

    public List<DataEntry> getData(Pattern regex) {
        return node.getData(address(), regex);
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

    public List<TransactionInfo> getTransactionsHistory() {
        return node.getTransactionsByAddress(address());
    }

    public List<TransactionInfo> getTransactionsHistory(int limit) {
        return node.getTransactionsByAddress(address(), limit);
    }

    public List<TransactionInfo> getTransactionsHistory(int limit, Id afterTxId) {
        return node.getTransactionsByAddress(address(), limit, afterTxId);
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

    private static <T extends TransactionOrOrder> T signAndGet(List<Account> signers, T unsigned) {
        for (Account account : signers)
            if (account == null)
                unsigned.addProof(Proof.EMPTY);
            else
                unsigned.addProof(account.privateKey());
        return unsigned;
    }

    /* TRANSACTIONS */

    public IssueTransactionInfo issue(Consumer<IssueParams> params) {
        IssueParams ip = new IssueParams(this);
        params.accept(ip);
        if (ip.signers.size() == 0)
            ip.signedBy(ip.sender);
        Transaction signedTx = signAndGet(ip.signers,
                IssueTransaction.builder(ip.name, ip.quantity, ip.decimals)
                        .description(ip.description)
                        .isReissuable(ip.reissuable)
                        .script(ip.compiledScript)
                        .sender(ip.sender.publicKey())
                        .fee(Amount.of(ip.getFee(), ip.feeAssetId))
                        .timestamp(ip.timestamp == 0 ? System.currentTimeMillis() : ip.timestamp)
                        .getUnsigned());
        return node.waitForTransaction(node.broadcast(signedTx).id(), IssueTransactionInfo.class);
    }

    public IssueTransactionInfo issue() {
        return issue(opt -> {});
    }

    public IssueTransactionInfo issueNft(Consumer<IssueNftParams> params) {
        IssueNftParams ip = new IssueNftParams(this);
        params.accept(ip);
        if (ip.signers.size() == 0)
            ip.signedBy(ip.sender);
        Transaction signedTx = signAndGet(ip.signers,
                IssueTransaction.builderNFT(ip.name)
                        .description(ip.description)
                        .script(ip.compiledScript)
                        .sender(ip.sender.publicKey())
                        .fee(Amount.of(ip.getFee(), ip.feeAssetId))
                        .timestamp(ip.timestamp == 0 ? System.currentTimeMillis() : ip.timestamp)
                        .getUnsigned());
        return node.waitForTransaction(node.broadcast(signedTx).id(), IssueTransactionInfo.class);
    }

    public IssueTransactionInfo issueNft() {
        return issueNft(i -> {});
    }

    public TransferTransactionInfo transfer(Recipient to, Amount amount, Consumer<TransferParams> params) {
        TransferParams tp = new TransferParams(this);
        params.accept(tp);
        if (tp.signers.size() == 0)
            tp.signedBy(tp.sender);
        tp.assetId(amount.assetId());
        Transaction signedTx = signAndGet(tp.signers,
                TransferTransaction.builder(to, amount)
                        .attachment(tp.attachment)
                        .sender(tp.sender.publicKey())
                        .fee(Amount.of(tp.getFee(), tp.feeAssetId))
                        .timestamp(tp.timestamp == 0 ? System.currentTimeMillis() : tp.timestamp)
                        .getUnsigned());
        Id txId = node.broadcast(signedTx).id();
        return node.waitForTransaction(txId, TransferTransactionInfo.class);
    }

    public TransferTransactionInfo transfer(Account to, Amount amount, Consumer<TransferParams> params) {
        return transfer(to.address(), amount, params);
    }

    public TransferTransactionInfo transfer(Account to, long amount, AssetId assetId, Consumer<TransferParams> params) {
        return transfer(to.address(), Amount.of(amount, assetId), params);
    }

    public TransferTransactionInfo transfer(Recipient to, Amount amount) {
        return transfer(to, amount, opt -> {});
    }

    public TransferTransactionInfo transfer(Account to, Amount amount) {
        return transfer(to.address(), amount, opt -> {});
    }

    public TransferTransactionInfo transfer(Account to, long amount, AssetId assetId) {
        return transfer(to.address(), Amount.of(amount, assetId), opt -> {});
    }

    public ReissueTransactionInfo reissue(Amount amount, Consumer<ReissueParams> params) {
        ReissueParams rp = new ReissueParams(this);
        params.accept(rp);
        if (rp.signers.size() == 0)
            rp.signedBy(rp.sender);
        rp.assetId(amount.assetId());
        Transaction signedTx = signAndGet(rp.signers,
                ReissueTransaction.builder(amount)
                        .reissuable(rp.reissuable)
                        .sender(rp.sender.publicKey())
                        .fee(Amount.of(rp.getFee(), rp.feeAssetId))
                        .timestamp(rp.timestamp == 0 ? System.currentTimeMillis() : rp.timestamp)
                        .getUnsigned());
        return node.waitForTransaction(node.broadcast(signedTx).id(), ReissueTransactionInfo.class);
    }

    public ReissueTransactionInfo reissue(long amount, AssetId assetId, Consumer<ReissueParams> params) {
        return reissue(Amount.of(amount, assetId), params);
    }

    public ReissueTransactionInfo reissue(Amount amount) {
        return reissue(amount, opt -> {});
    }

    public ReissueTransactionInfo reissue(long amount, AssetId assetId) {
        return reissue(amount, assetId, opt -> {});
    }

    public BurnTransactionInfo burn(Amount amount, Consumer<CommonParams<?>> params) {
        BurnParams common = new BurnParams(this);
        params.accept(common);
        if (common.signers.size() == 0)
            common.signedBy(common.sender);
        common.assetId(amount.assetId());
        Transaction signedTx = signAndGet(common.signers,
                BurnTransaction.builder(amount)
                        .sender(common.sender.publicKey())
                        .fee(Amount.of(common.getFee(), common.feeAssetId))
                        .timestamp(common.timestamp == 0 ? System.currentTimeMillis() : common.timestamp)
                        .getUnsigned());
        return node.waitForTransaction(node.broadcast(signedTx).id(), BurnTransactionInfo.class);
    }

    public BurnTransactionInfo burn(long amount, AssetId assetId, Consumer<CommonParams<?>> params) {
        return burn(Amount.of(amount, assetId), params);
    }

    public BurnTransactionInfo burn(Amount amount) {
        return burn(amount, opt -> {});
    }

    public BurnTransactionInfo burn(long amount, AssetId assetId) {
        return burn(amount, assetId, opt -> {});
    }

    public ExchangeTransactionInfo exchange(Order order1, Order order2, long amount, long price, Consumer<ExchangeParams> params) {
        ExchangeParams ep = new ExchangeParams(this);
        params.accept(ep);
        if (ep.signers.size() == 0)
            ep.signedBy(ep.sender);
        ep.assetPair(order1.assetPair());
        Transaction signedTx = signAndGet(ep.signers,
                ExchangeTransaction.builder(order1, order2, amount, price, ep.buyMatcherFee, ep.sellMatcherFee)
                        .sender(ep.sender.publicKey())
                        .fee(Amount.of(ep.getFee(), ep.feeAssetId))
                        .timestamp(ep.timestamp == 0 ? System.currentTimeMillis() : ep.timestamp)
                        .getUnsigned());
        return node.waitForTransaction(node.broadcast(signedTx).id(), ExchangeTransactionInfo.class);
    }

    public ExchangeTransactionInfo exchange(Order order1, Order order2, long amount, long price) {
        return exchange(order1, order2, amount, price, opt -> {});
    }

    public LeaseTransactionInfo lease(Recipient to, long amount, Consumer<CommonParams<?>> params) {
        CommonParams<?> common = new CommonParams<>(this, LeaseTransaction.MIN_FEE);
        params.accept(common);
        if (common.signers.size() == 0)
            common.signedBy(common.sender);
        Transaction signedTx = signAndGet(common.signers,
                LeaseTransaction.builder(to, amount)
                        .sender(common.sender.publicKey())
                        .fee(Amount.of(common.getFee(), common.feeAssetId))
                        .timestamp(common.timestamp == 0 ? System.currentTimeMillis() : common.timestamp)
                        .getUnsigned());
        return node.waitForTransaction(node.broadcast(signedTx).id(), LeaseTransactionInfo.class);
    }

    public LeaseTransactionInfo lease(Recipient to, long amount) {
        return lease(to, amount, opt -> {});
    }

    public LeaseTransactionInfo lease(Account to, long amount, Consumer<CommonParams<?>> params) {
        return lease(to.address(), amount, params);
    }

    public LeaseTransactionInfo lease(Account to, long amount) {
        return lease(to.address(), amount, opt -> {});
    }

    public LeaseCancelTransactionInfo cancelLease(Id leaseId, Consumer<CommonParams<?>> params) {
        CommonParams<?> common = new CommonParams<>(this, LeaseCancelTransaction.MIN_FEE);
        params.accept(common);
        if (common.signers.size() == 0)
            common.signedBy(common.sender);
        Transaction signedTx = signAndGet(common.signers,
                LeaseCancelTransaction.builder(leaseId)
                        .sender(common.sender.publicKey())
                        .fee(Amount.of(common.getFee(), common.feeAssetId))
                        .timestamp(common.timestamp == 0 ? System.currentTimeMillis() : common.timestamp)
                        .getUnsigned());
        return node.waitForTransaction(node.broadcast(signedTx).id(), LeaseCancelTransactionInfo.class);
    }

    public LeaseCancelTransactionInfo cancelLease(Id leaseId) {
        return cancelLease(leaseId, opt -> {});
    }

    public CreateAliasTransactionInfo createAlias(String alias, Consumer<CommonParams<?>> params) {
        CommonParams<?> common = new CommonParams<>(this, CreateAliasTransaction.MIN_FEE);
        params.accept(common);
        if (common.signers.size() == 0)
            common.signedBy(common.sender);
        Transaction signedTx = signAndGet(common.signers,
                CreateAliasTransaction.builder(alias)
                        .sender(common.sender.publicKey())
                        .fee(Amount.of(common.getFee(), common.feeAssetId))
                        .timestamp(common.timestamp == 0 ? System.currentTimeMillis() : common.timestamp)
                        .getUnsigned());
        return node.waitForTransaction(node.broadcast(signedTx).id(), CreateAliasTransactionInfo.class);
    }

    public CreateAliasTransactionInfo createAlias(String alias) {
        return createAlias(alias, opt -> {});
    }

    public CreateAliasTransactionInfo createAlias(Alias alias) {
        return createAlias(alias.toString(), opt -> {});
    }

    public MassTransferTransactionInfo massTransfer(Consumer<MassTransferParams> params) {
        MassTransferParams mtp = new MassTransferParams(this);
        params.accept(mtp);
        if (mtp.signers.size() == 0)
            mtp.signedBy(mtp.sender);
        Transaction signedTx = signAndGet(mtp.signers,
                MassTransferTransaction.builder(mtp.transfers)
                        .assetId(mtp.assetId)
                        .attachment(mtp.attachment)
                        .sender(mtp.sender.publicKey())
                        .fee(Amount.of(mtp.getFee(), mtp.feeAssetId))
                        .timestamp(mtp.timestamp == 0 ? System.currentTimeMillis() : mtp.timestamp)
                        .getUnsigned());
        return node.waitForTransaction(node.broadcast(signedTx).id(), MassTransferTransactionInfo.class);
    }

    public DataTransactionInfo writeData(Consumer<DataParams> params) {
        DataParams dp = new DataParams(this);
        params.accept(dp);
        if (dp.signers.size() == 0)
            dp.signedBy(dp.sender);
        Transaction signedTx = signAndGet(dp.signers,
                DataTransaction.builder(dp.data)
                        .sender(dp.sender.publicKey())
                        .fee(Amount.of(dp.getFee(), dp.feeAssetId))
                        .timestamp(dp.timestamp == 0 ? System.currentTimeMillis() : dp.timestamp)
                        .getUnsigned());
        return node.waitForTransaction(node.broadcast(signedTx).id(), DataTransactionInfo.class);
    }

    public SetScriptTransactionInfo setScript(Base64String compiledScript, Consumer<CommonParams<?>> params) {
        SetScriptParams common = new SetScriptParams(this);
        params.accept(common);
        if (common.signers.size() == 0)
            common.signedBy(common.sender);
        common.compiledScript(compiledScript);
        Transaction signedTx = signAndGet(common.signers,
                SetScriptTransaction.builder(compiledScript)
                        .sender(common.sender.publicKey())
                        .fee(Amount.of(common.getFee(), common.feeAssetId))
                        .timestamp(common.timestamp == 0 ? System.currentTimeMillis() : common.timestamp)
                        .getUnsigned());
        return node.waitForTransaction(node.broadcast(signedTx).id(), SetScriptTransactionInfo.class);
    }

    public SetScriptTransactionInfo setScript(String script, Consumer<CommonParams<?>> params) {
        return setScript(node.compileScript(script).script(), params);
    }

    public SetScriptTransactionInfo setScript(String script, boolean enableCompaction, Consumer<CommonParams<?>> params) {
        return setScript(node.compileScript(script, enableCompaction).script(), params);
    }

    public SetScriptTransactionInfo setScript(Base64String compiledScript) {
        return setScript(compiledScript, opt -> {});
    }

    public SetScriptTransactionInfo setScript(String script) {
        return setScript(script, opt -> {});
    }

    public SetScriptTransactionInfo setScript(String script, boolean enableCompaction) {
        return setScript(script, enableCompaction, opt -> {});
    }

    public SponsorFeeTransactionInfo sponsorFee(AssetId assetId, long minFeeAmount, Consumer<CommonParams<?>> params) {
        CommonParams<?> common = new CommonParams<>(this, SponsorFeeTransaction.MIN_FEE);
        params.accept(common);
        if (common.signers.size() == 0)
            common.signedBy(common.sender);
        Transaction signedTx = signAndGet(common.signers,
                SponsorFeeTransaction.builder(assetId, minFeeAmount)
                        .sender(common.sender.publicKey())
                        .fee(Amount.of(common.getFee(), common.feeAssetId))
                        .timestamp(common.timestamp == 0 ? System.currentTimeMillis() : common.timestamp)
                        .getUnsigned());
        return node.waitForTransaction(node.broadcast(signedTx).id(), SponsorFeeTransactionInfo.class);
    }

    public SponsorFeeTransactionInfo sponsorFee(Amount amount, Consumer<CommonParams<?>> params) {
        return sponsorFee(amount.assetId(), amount.value(), params);
    }

    public SponsorFeeTransactionInfo sponsorFee(AssetId assetId, long minFeeAmount) {
        return sponsorFee(assetId, minFeeAmount, opt -> {});
    }

    public SponsorFeeTransactionInfo sponsorFee(Amount amount) {
        return sponsorFee(amount.assetId(), amount.value(), opt -> {});
    }

    public SetAssetScriptTransactionInfo setAssetScript(AssetId assetId, Base64String compiledScript, Consumer<CommonParams<?>> params) {
        CommonParams<?> common = new CommonParams<>(this, SetAssetScriptTransaction.MIN_FEE);
        params.accept(common);
        if (common.signers.size() == 0)
            common.signedBy(common.sender);
        Transaction signedTx = signAndGet(common.signers,
                SetAssetScriptTransaction.builder(assetId, compiledScript)
                        .sender(common.sender.publicKey())
                        .fee(Amount.of(common.getFee(), common.feeAssetId))
                        .timestamp(common.timestamp == 0 ? System.currentTimeMillis() : common.timestamp)
                        .getUnsigned());
        return node.waitForTransaction(node.broadcast(signedTx).id(), SetAssetScriptTransactionInfo.class);
    }

    public SetAssetScriptTransactionInfo setAssetScript(AssetId assetId, String script, Consumer<CommonParams<?>> params) {
        return setAssetScript(assetId, node.compileScript(Script.setAssetType(script)).script(), params);
    }

    public SetAssetScriptTransactionInfo setAssetScript(AssetId assetId, Base64String compiledScript) {
        return setAssetScript(assetId, compiledScript, opt -> {});
    }

    public SetAssetScriptTransactionInfo setAssetScript(AssetId assetId, String script) {
        return setAssetScript(assetId, script, opt -> {});
    }

    public InvokeScriptTransactionInfo invoke(Consumer<InvokeScriptParams> params) {
        InvokeScriptParams isp = new InvokeScriptParams(this);
        params.accept(isp);
        if (isp.signers.size() == 0)
            isp.signedBy(isp.sender);
        Transaction signedTx = signAndGet(isp.signers,
                InvokeScriptTransaction.builder(isp.dApp, isp.functions).payments(isp.payments)
                        .sender(isp.sender.publicKey())
                        .fee(Amount.of(isp.getFee(), isp.feeAssetId))
                        .timestamp(isp.timestamp == 0 ? System.currentTimeMillis() : isp.timestamp)
                        .getUnsigned());
        return node.waitForTransaction(node.broadcast(signedTx).id(), InvokeScriptTransactionInfo.class);
    }

    public InvokeScriptTransactionInfo invoke(DAppCall call, Consumer<InvokeScriptParamsOptional> params) {
        InvokeScriptParamsOptional ispOpt = new InvokeScriptParamsOptional(this);
        params.accept(ispOpt);
        return invoke(is -> is
                .dApp(call.getDApp())
                .function(call.getFunction())
                .payments(ispOpt.payments.toArray(new Amount[0]))
                .additionalFee(ispOpt.additionalFee)
                .feeAssetId(ispOpt.feeAssetId)
                .timestamp(ispOpt.timestamp)
                .signedBy(ispOpt.signers.toArray(new Account[0])));
    }

    public InvokeScriptTransactionInfo invoke(DAppCall call, Amount... payments) {
        return invoke(i -> i.dApp(call.getDApp()).function(call.getFunction()).payments(payments));
    }

    public UpdateAssetInfoTransactionInfo updateAssetInfo(AssetId assetId, String name, String description, Consumer<UpdateAssetInfoParams> params) {
        UpdateAssetInfoParams uap = new UpdateAssetInfoParams(this);
        params.accept(uap);
        if (uap.signers.size() == 0)
            uap.signedBy(uap.sender);
        uap.assetId(assetId);
        Transaction signedTx = signAndGet(uap.signers,
                UpdateAssetInfoTransaction.builder(assetId, name, description)
                        .sender(uap.sender.publicKey())
                        .fee(Amount.of(uap.getFee(), uap.feeAssetId))
                        .timestamp(uap.timestamp == 0 ? System.currentTimeMillis() : uap.timestamp)
                        .getUnsigned());
        return node.waitForTransaction(node.broadcast(signedTx).id(), UpdateAssetInfoTransactionInfo.class);
    }

    public UpdateAssetInfoTransactionInfo updateAssetInfo(AssetId assetId, String name, String description) {
        return updateAssetInfo(assetId, name, description, opt -> {});
    }

}
