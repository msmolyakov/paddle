package im.mak.paddle.params;

import im.mak.paddle.Account;
import com.wavesplatform.transactions.InvokeScriptTransaction;
import com.wavesplatform.transactions.common.Amount;
import com.wavesplatform.transactions.common.AssetId;
import com.wavesplatform.transactions.common.Recipient;
import com.wavesplatform.transactions.invocation.Arg;
import com.wavesplatform.transactions.invocation.Function;
import im.mak.paddle.token.Token;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static im.mak.paddle.Node.node;

public class InvokeScriptParams extends CommonParams<InvokeScriptParams> {

    protected Recipient dApp;
    protected Function call;
    protected List<Amount> payments;

    public InvokeScriptParams(Account sender) {
        super(sender, InvokeScriptTransaction.MIN_FEE);

        this.dApp = sender.address();
        defaultFunction();
        this.payments = new ArrayList<>();
        this.feeAssetId = AssetId.WAVES;
    }

    public InvokeScriptParams dApp(Recipient addressOrAlias) {
        this.dApp = addressOrAlias;
        return this;
    }

    public InvokeScriptParams dApp(Account account) {
        return dApp(account.address());
    }

    //TODO accept lambda consumer of args. Do the same for DataParams
    public InvokeScriptParams function(Function call) {
        this.call = call;
        return this;
    }
    public InvokeScriptParams function(String name, Arg... args) {
        return function(Function.as(name, args));
    }

    public InvokeScriptParams defaultFunction() {
        this.call = Function.asDefault();
        return this;
    }

    public InvokeScriptParams payments(Amount... amount) {
        this.payments.addAll(Arrays.asList(amount));
        return this;
    }

    public InvokeScriptParams payment(Amount amount) {
        this.payments.add(amount);
        return this;
    }

    public InvokeScriptParams payment(long amount, AssetId assetId) {
        return payment(Amount.of(amount, assetId));
    }

    public InvokeScriptParams payment(long amount, Token token) {
        return payment(amount, token.id());
    }

    public InvokeScriptParams wavesPayment(long amount) {
        return payment(amount, AssetId.WAVES);
    }

    public InvokeScriptParams additionalFee(long amount, AssetId assetId) {
        this.feeAssetId = assetId;

        if (this.feeAssetId.isWaves()) {
            this.additionalFee = amount;
            return this;
        } else {
            long sponsoredMinFee = node().getAssetDetails(this.feeAssetId).minSponsoredAssetFee();
            return additionalFee(amount * sponsoredMinFee);
        }
    }

    public InvokeScriptParams additionalFee(long amount, Token token) {
        return this.additionalFee(amount, token.id());
    }

    public InvokeScriptParams additionalFee(Amount amount) {
        return this.additionalFee(amount.value(), amount.assetId());
    }

    @Override
    public InvokeScriptParams additionalFee(long amount) {
        return this.additionalFee(amount, this.feeAssetId);
    }

    public InvokeScriptParams feeAssetId(AssetId assetId) {
        this.feeAssetId = assetId;
        return this;
    }

    public InvokeScriptParams feeAsset(Token token) {
        return feeAssetId(token.id());
    }

}
