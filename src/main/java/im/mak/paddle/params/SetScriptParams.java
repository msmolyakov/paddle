package im.mak.paddle.params;

import im.mak.paddle.Account;
import com.wavesplatform.transactions.SetScriptTransaction;
import com.wavesplatform.transactions.common.Base64String;

import static im.mak.paddle.Node.node;

public class SetScriptParams extends CommonParams<SetScriptParams> {

    protected Base64String compiledScript;

    public SetScriptParams(Account sender) {
        super(sender, SetScriptTransaction.MIN_FEE);
    }

    public SetScriptParams compiledScript(Base64String compiled) {
        this.compiledScript = compiled;
        return this;
    }

    public SetScriptParams script(String sourceCode) {
        return compiledScript(node().compileScript(sourceCode).script());
    }

}
