package im.mak.paddle.actions;

import com.wavesplatform.wavesj.transactions.InvokeScriptTransaction;
import com.wavesplatform.wavesj.transactions.InvokeScriptTransaction.FunctionCall;
import com.wavesplatform.wavesj.transactions.InvokeScriptTransaction.Payment;
import im.mak.paddle.Account;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static im.mak.paddle.Constants.EXTRA_FEE;
import static im.mak.paddle.Constants.MIN_FEE;

public class InvokeScript implements Action {

    public Account sender;
    public String dApp;
    public FunctionCall call;
    public List<Payment> payments;
    public long fee;
    public String feeAssetId;

    public InvokeScript(Account from) {
        this.sender = from;
        this.dApp = from.address();

        this.call = null;
        this.payments = new ArrayList<>();
        this.fee = 0;
        this.feeAssetId = "WAVES";
    }

    public static InvokeScript invokeScript(Account from) {
        return new InvokeScript(from);
    }

    public InvokeScript dApp(String addressOrAlias) {
        this.dApp = addressOrAlias;
        return this;
    }

    public InvokeScript dApp(Account account) {
        return dApp(account.address());
    }

    public InvokeScript function(String name, InvokeScriptTransaction.FunctionalArg... args) {
        this.call = new FunctionCall(name);
        Arrays.stream(args).forEach(arg -> this.call.addArg(arg));
        return this;
    }

    public InvokeScript defaultFunction() {
        this.call = null;
        return this;
    }

    public InvokeScript payment(long amount, String assetId) {
        //TODO several payments
        this.payments.add(new Payment(amount, assetId));
        return this;
    }

    public InvokeScript wavesPayment(long amount) {
        //TODO several payments
        return payment(amount, null);
    }

    public InvokeScript fee(long fee) {
        this.fee = fee;
        return this;
    }

    /**
     * Важно! Не учитывает переводы смарт ассетов через TransferSet.
     * В таком случае комиссию можно указывать самостоятельно: `invoke.fee(invoke.calcFee() + EXTRA_FEE)`
     */
    @Override
    public long calcFee() {
        if (this.fee > 0) {
            return this.fee;
        } else {
            long totalFee = MIN_FEE + EXTRA_FEE;
            totalFee += sender.isSmart() ? EXTRA_FEE : 0;
            for (Payment pmt : payments)
                totalFee += sender.node.isSmart(pmt.getAssetId()) ? EXTRA_FEE : 0;
            return totalFee;
        }
    }

}
