package im.mak.paddle.dapp;

import com.wavesplatform.transactions.common.Recipient;
import com.wavesplatform.transactions.invocation.Function;

public class DAppCall {

    private final Recipient dApp;
    private final Function function;

    public DAppCall(Recipient addressOrAlias, Function function) {
        this.dApp = addressOrAlias;
        this.function = function;
    }

    public Recipient getDApp() {
        return this.dApp;
    }

    public Function getFunction() {
        return this.function;
    }

}
