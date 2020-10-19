package im.mak.paddle.actions;

import com.wavesplatform.wavesj.exceptions.NodeException;
import im.mak.paddle.Account;
import im.mak.waves.transactions.SetScriptTransaction;
import im.mak.waves.transactions.common.Base64String;

import java.io.IOException;

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
