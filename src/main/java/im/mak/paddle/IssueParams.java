package im.mak.paddle;

import com.wavesplatform.transactions.IssueTransaction;
import com.wavesplatform.transactions.common.Base64String;
import im.mak.paddle.util.Script;

import java.util.Random;

import static im.mak.paddle.Node.node;

public class IssueParams extends CommonParams<IssueParams> {

    protected String name;
    protected String description;
    protected long quantity;
    protected int decimals;
    protected boolean reissuable;
    protected Base64String compiledScript;

    protected IssueParams(Account sender) {
        super(sender, IssueTransaction.MIN_FEE);

        this.name = "Asset " + new Random().nextInt(10000);
        this.description = "";
        this.quantity = 100000000_00000000L;
        this.decimals = 8;
        this.reissuable = true;
    }

    public IssueParams name(String name) {
        this.name = name;
        return this;
    }

    public IssueParams description(String description) {
        this.description = description;
        return this;
    }

    public IssueParams quantity(long quantity) {
        this.quantity = quantity;
        return this;
    }

    public IssueParams decimals(int value) {
        this.decimals = value;
        return this;
    }

    public IssueParams reissuable(boolean reissuable) {
        this.reissuable = reissuable;
        return this;
    }

    public IssueParams compiledScript(Base64String compiled) {
        this.compiledScript = compiled;
        return this;
    }

    public IssueParams script(String sourceCode) {
        if (sourceCode == null)
            return compiledScript(Base64String.empty());

        String script = Script.setAssetType(sourceCode);
        return compiledScript(node().compileScript(script).script());
    }

}
