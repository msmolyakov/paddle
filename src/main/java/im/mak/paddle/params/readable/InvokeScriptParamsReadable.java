package im.mak.paddle.params.readable;

import com.wavesplatform.transactions.common.Amount;
import com.wavesplatform.transactions.common.AssetId;
import com.wavesplatform.transactions.common.Recipient;
import com.wavesplatform.transactions.invocation.Function;
import com.wavesplatform.wavesj.ScriptInfo;
import im.mak.paddle.Account;
import im.mak.paddle.params.InvokeScriptParams;
import im.mak.paddle.util.RecipientResolver;
import im.mak.paddle.util.Script;

import java.util.List;

import static im.mak.paddle.Node.node;
import static im.mak.paddle.util.Constants.EXTRA_FEE;
import static im.mak.paddle.util.Constants.MIN_FEE;

public class InvokeScriptParamsReadable extends InvokeScriptParams {

    public InvokeScriptParamsReadable(Account sender) {
        super(sender);
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

        ScriptInfo scriptInfo = node().getScriptInfo(RecipientResolver.toAddress(dApp));
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

    /* COMMON PARAMS */

    @Override
    public Account getSender() {
        return super.getSender();
    }

    @Override
    public long getTimestamp() {
        return super.getTimestamp();
    }

    @Override
    public AssetId getFeeAssetId() {
        return super.getFeeAssetId();
    }

    @Override
    public List<Object> getSignersAndProofs() {
        return super.getSignersAndProofs();
    }

}
