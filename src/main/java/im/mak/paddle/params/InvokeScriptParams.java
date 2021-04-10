package im.mak.paddle.params;

import com.wavesplatform.transactions.account.Address;
import com.wavesplatform.transactions.common.Alias;
import com.wavesplatform.wavesj.ScriptInfo;
import im.mak.paddle.Account;
import com.wavesplatform.transactions.InvokeScriptTransaction;
import com.wavesplatform.transactions.common.Amount;
import com.wavesplatform.transactions.common.AssetId;
import com.wavesplatform.transactions.common.Recipient;
import com.wavesplatform.transactions.invocation.Arg;
import com.wavesplatform.transactions.invocation.Function;
import im.mak.paddle.util.Script;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static im.mak.paddle.Constants.EXTRA_FEE;
import static im.mak.paddle.Constants.MIN_FEE;
import static im.mak.paddle.Node.node;

public class InvokeScriptParams extends TxParams<InvokeScriptParams> {

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
    public InvokeScriptParams function(String name, Arg... args) {
        this.call = Function.as(name, args);
        return this;
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

    public Recipient getDApp() {
        return this.dApp;
    }

    public Function getCall() {
        return this.call;
    }

    public List<Amount> getPayments() {
        return this.payments;
    }

    /**
     * Important! Does not consider actions with smart assets in the invoked script.
     * Also it does not consider Issue actions in the invoked script.
     * In this case, the commission can be specified independently:
     * `invoke.extraFee(EXTRA_FEE)`
     */
    @Override
    public long getFee() {
        long totalWavesFee = super.getFee();

        ScriptInfo scriptInfo = node().getScriptInfo(
                dApp.type() == 1 ? (Address) dApp : node().getAddressByAlias((Alias) dApp));
        int rideVersion = Script.getRideVersion(scriptInfo.script());

        //TODO just request /debug/validate to consider Issue actions and actions with smart assets; remove javadoc
        if (rideVersion <= 4) {
            for (Amount payment : payments)
                if (!payment.assetId().isWaves())
                    totalWavesFee += node().getAssetDetails(payment.assetId()).isScripted() ? EXTRA_FEE : 0;
        }

        if (feeAssetId.isWaves())
            return totalWavesFee;
        else {
            long minSponsoredFee = node().getAssetDetails(feeAssetId).minSponsoredAssetFee();
            long increment = totalWavesFee % MIN_FEE == 0 ? 0 : minSponsoredFee;
            return (totalWavesFee / MIN_FEE) * minSponsoredFee + increment;
        }
    }

}
