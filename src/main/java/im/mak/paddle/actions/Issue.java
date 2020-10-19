package im.mak.paddle.actions;

import com.wavesplatform.wavesj.exceptions.NodeException;
import im.mak.paddle.Account;
import im.mak.waves.transactions.IssueTransaction;
import im.mak.waves.transactions.common.Base64String;

import java.io.IOException;
import java.util.Random;

import static im.mak.paddle.Node.node;

public class Issue extends Action<Issue> {

    public String name;
    public String description;
    public long quantity;
    public int decimals;
    public boolean reissuable;
    public Base64String compiledScript;

    public Issue(Account sender) {
        super(sender, IssueTransaction.MIN_FEE);

        this.name = "Asset " + new Random().nextInt(1000);
        this.description = "";
        this.quantity = 10000000000000000L;
        this.decimals = 8;
        this.reissuable = true;
    }

    public Issue name(String name) {
        this.name = name;
        return this;
    }

    public Issue description(String description) {
        this.description = description;
        return this;
    }

    public Issue quantity(long quantity) {
        this.quantity = quantity;
        return this;
    }

    public Issue decimals(int value) {
        this.decimals = value;
        return this;
    }

    public Issue reissuable(boolean reissuable) {
        this.reissuable = reissuable;
        return this;
    }

    //TODO rework such methods
    public Issue script(String sourceCode) {
        this.compiledScript = sourceCode == null ? Base64String.empty() : node().compileScript(sourceCode).script();
        return this;
    }

}
