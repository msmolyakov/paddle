package im.mak.paddle;

import com.wavesplatform.transactions.IssueTransaction;
import com.wavesplatform.transactions.common.Base64String;
import im.mak.paddle.util.Script;

import java.util.Random;

import static im.mak.paddle.Node.node;

public class IssueNftParams extends CommonParams<IssueNftParams> {

    protected String name;
    protected String description;
    protected Base64String compiledScript;

    protected IssueNftParams(Account sender) {
        super(sender, IssueTransaction.NFT_MIN_FEE);

        this.name = "NFT " + new Random().nextInt(1000000);
        this.description = "";
    }

    public IssueNftParams name(String name) {
        this.name = name;
        return this;
    }

    public IssueNftParams description(String description) {
        this.description = description;
        return this;
    }

    public IssueNftParams compiledScript(Base64String compiled) {
        this.compiledScript = compiled == null ? Base64String.empty() : compiled;
        return this;
    }

    public IssueNftParams script(String sourceCode) {
        if (sourceCode == null)
            return compiledScript(Base64String.empty());

        String script = Script.setAssetType(sourceCode);
        return compiledScript(node().compileScript(script).script());
    }

}
