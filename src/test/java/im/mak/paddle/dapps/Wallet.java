package im.mak.paddle.dapps;

import com.wavesplatform.transactions.common.Recipient;
import com.wavesplatform.transactions.invocation.Function;
import com.wavesplatform.transactions.invocation.IntegerArg;
import im.mak.paddle.invocation.DApp;
import im.mak.paddle.invocation.DAppCall;

public class Wallet extends DApp {

    public Wallet(Recipient addressOrAlias) {
        super(addressOrAlias);
    }

    public DAppCall deposit() {
        return new DAppCall(this.getDApp(), Function.as("deposit"));
    }

    public DAppCall withdraw(long amount) {
        return new DAppCall(this.getDApp(), Function.as("withdraw", IntegerArg.as(amount)));
    }

}
