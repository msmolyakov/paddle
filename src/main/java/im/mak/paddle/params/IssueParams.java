package im.mak.paddle.params;

import im.mak.paddle.Account;
import com.wavesplatform.transactions.IssueTransaction;
import com.wavesplatform.transactions.common.Base64String;

import java.util.Random;

import static im.mak.paddle.Node.node;

public class IssueParams extends TxParams<IssueParams> {

    protected String name;
    protected String description;
    protected long quantity;
    protected int decimals;
    protected boolean reissuable;
    protected Base64String compiledScript;

    public IssueParams(Account sender) {
        super(sender, IssueTransaction.MIN_FEE);

        this.name = "Asset " + new Random().nextInt(1000);
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
        return compiledScript(sourceCode == null ? null : node().compileScript(sourceCode).script());
    }

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    public long getQuantity() {
        return this.quantity;
    }

    public int getDecimals() {
        return this.decimals;
    }

    public boolean isReissuable() {
        return this.reissuable;
    }

    public Base64String getCompiledScript() {
        return this.compiledScript;
    }

}
