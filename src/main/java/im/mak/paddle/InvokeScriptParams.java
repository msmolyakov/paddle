package im.mak.paddle;

import com.wavesplatform.transactions.common.Amount;
import com.wavesplatform.transactions.common.Recipient;
import com.wavesplatform.transactions.invocation.Arg;
import com.wavesplatform.transactions.invocation.Function;
import com.wavesplatform.wavesj.ScriptInfo;
import im.mak.paddle.util.RecipientResolver;
import im.mak.paddle.util.Script;

import static im.mak.paddle.Node.node;
import static im.mak.paddle.util.Constants.EXTRA_FEE;
import static im.mak.paddle.util.Constants.MIN_FEE;

public class InvokeScriptParams extends InvokeScriptParamsOptional {

    protected Recipient dApp;
    protected Function functions;

    protected InvokeScriptParams(Account sender) {
        super(sender);

        this.dApp = sender.address();
        defaultFunction();
    }

    /**
     * Important! Does not consider actions with smart assets in the invoked script.
     * Also it does not consider Issue actions in the invoked script.
     * In this case, the commission can be specified independently:
     * `invoke.extraFee(EXTRA_FEE)`
     */
    @Override
    protected long getFee() {
        long totalWavesFee = super.getFee();

        ScriptInfo scriptInfo = node().getScriptInfo(RecipientResolver.toAddress(dApp));
        int rideVersion = Script.getRideVersion(scriptInfo.script());

        //TODO just request /debug/validate to consider Issue actions and actions with smart assets; remove javadoc
        if (rideVersion <= 4) {
            for (Amount payment : payments)
                if (!payment.assetId().isWaves() && node().getAssetDetails(payment.assetId()).isScripted())
                    totalWavesFee += EXTRA_FEE;
        }

        if (feeAssetId.isWaves())
            return totalWavesFee;
        else {
            long minSponsoredFee = node().getAssetDetails(feeAssetId).minSponsoredAssetFee();
            long increment = totalWavesFee % MIN_FEE == 0 ? 0 : minSponsoredFee;
            return (totalWavesFee / MIN_FEE) * minSponsoredFee + increment;
        }
    }

    public InvokeScriptParams dApp(Recipient addressOrAlias) {
        this.dApp = addressOrAlias;
        return this;
    }

    public InvokeScriptParams dApp(Account account) {
        return dApp(account.address());
    }

    public InvokeScriptParams function(Function function) {
        this.functions = function;
        return this;
    }
    public InvokeScriptParams function(String name, Arg... args) {
        return function(Function.as(name, args));
    }

    public InvokeScriptParams defaultFunction() {
        this.functions = Function.asDefault();
        return this;
    }

}
