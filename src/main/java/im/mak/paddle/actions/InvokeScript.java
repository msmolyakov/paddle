package im.mak.paddle.actions;

import com.wavesplatform.wavesj.exceptions.NodeException;
import im.mak.paddle.Account;
import im.mak.waves.transactions.InvokeScriptTransaction;
import im.mak.waves.transactions.common.Amount;
import im.mak.waves.transactions.common.AssetId;
import im.mak.waves.transactions.common.Recipient;
import im.mak.waves.transactions.invocation.Arg;
import im.mak.waves.transactions.invocation.Function;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static im.mak.paddle.Constants.EXTRA_FEE;
import static im.mak.paddle.Constants.MIN_FEE;
import static im.mak.paddle.Node.node;

public class InvokeScript extends Action<InvokeScript> {

    public Recipient dApp;
    public Function call;
    public List<Amount> payments;
    public AssetId feeAssetId;

    public InvokeScript(Account sender) {
        super(sender, InvokeScriptTransaction.MIN_FEE);

        this.dApp = sender.address();
        this.call = Function.asDefault();
        this.payments = new ArrayList<>();
        this.feeAssetId = AssetId.WAVES;
    }

    public static InvokeScript invokeScript(Account from) {
        return new InvokeScript(from);
    }

    public InvokeScript dApp(Recipient addressOrAlias) {
        this.dApp = addressOrAlias;
        return this;
    }

    public InvokeScript dApp(Account account) {
        return dApp(account.address());
    }

    public InvokeScript function(String name, Arg... args) {
        this.call = Function.as(name, args);
        return this;
    }

    public InvokeScript defaultFunction() {
        this.call = Function.asDefault();
        return this;
    }

    public InvokeScript payment(Amount amount) {
        this.payments.add(amount);
        return this;
    }

    public InvokeScript payment(long amount, AssetId assetId) {
        return payment(Amount.of(amount, assetId));
    }

    public InvokeScript wavesPayment(long amount) {
        return payment(amount, AssetId.WAVES);
    }

    public InvokeScript fee(long amount, AssetId assetId) {
        this.feeAmount = amount;
        this.feeAssetId = assetId;
        return this;
    }

    public InvokeScript fee(Amount fee) {
        return fee(fee.value(), fee.assetId());
    }

    public InvokeScript fee(AssetId assetId) {
        this.feeAssetId = assetId;
        return this;
    }

    /**
     * Important! Does not consider actions with smart assets in the invoked script.
     * Also it does not consider Issue actions in the invoked script.
     * In this case, the commission can be specified independently:
     * `invoke.extraFee(EXTRA_FEE)`
     */
    @Override
    public long calcFee() {
        if (feeAmount > 0)
            return feeAmount;

        long totalWavesFee = super.calcFee();
        for (Amount payment : payments)
            if (!payment.assetId().isWaves())
                totalWavesFee += node().getAssetDetails(payment.assetId()).isScripted() ? EXTRA_FEE : 0;

        if (feeAssetId.isWaves())
            return totalWavesFee;

        long sponsoredMinAssetFee = node().getAssetDetails(feeAssetId).minSponsoredAssetFee();
        return sponsoredMinAssetFee * (long) Math.ceil((double) totalWavesFee / MIN_FEE);
    }

}
