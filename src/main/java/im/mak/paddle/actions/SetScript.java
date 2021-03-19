package im.mak.paddle.actions;

import im.mak.paddle.Account;
import com.wavesplatform.transactions.SetScriptTransaction;
import com.wavesplatform.transactions.common.Base64String;

import static im.mak.paddle.Node.node;

public class SetScript extends Action<SetScript> {

    public Base64String compiledScript;

    public SetScript(Account sender) {
        super(sender, SetScriptTransaction.MIN_FEE);
    }

    public SetScript compiledScript(Base64String compiled) {
        this.compiledScript = compiled;
        return this;
    }

    public SetScript script(String sourceCode) {
        return compiledScript(node().compileScript(sourceCode).script());
    }

}
