package im.mak.paddle.invocation;

import com.wavesplatform.transactions.common.Recipient;

//TODO extends Account?
public abstract class DApp {

    private final Recipient dApp;

    public DApp(Recipient addressOrAlias) {
        this.dApp = addressOrAlias;
    }

    public Recipient getDApp() {
        return this.dApp;
    }

}
