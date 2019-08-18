package im.mak.paddle.actions;

import im.mak.paddle.Account;
import im.mak.paddle.exceptions.NodeError;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Random;

import static im.mak.paddle.Constants.EXTRA_FEE;
import static im.mak.paddle.Constants.ONE_WAVES;
import static java.nio.file.Files.readAllLines;

public class Issue implements Action {

    public Account sender;
    public String name;
    public String description;
    public long quantity;
    public byte decimals;
    public boolean isReissuable;
    public String compiledScript;
    public long fee;

    public Issue(Account from) {
        this.sender = from;

        this.name = "Asset " + new Random().nextInt(10000);
        this.description = "";
        this.quantity = 10000000000000000L;
        this.decimals = 8;
        this.isReissuable = true;
        this.fee = 0;
    }

    public static Issue issue(Account from) {
        return new Issue(from);
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
        this.decimals = (byte) value;
        return this;
    }

    public Issue reissuable(boolean isReissuable) {
        this.isReissuable = isReissuable;
        return this;
    }

    public Issue reissuable() {
        return reissuable(true);
    }

    public Issue notReissuable() {
        return reissuable(false);
    }

    public Issue script(String sourceCode) {
        this.compiledScript = sourceCode == null ? null : this.sender.node.compileScript(sourceCode);
        return this;
    }

    public Issue script(Path filePath) {
        try {
            return script(String.join("\n", readAllLines(filePath)));
        } catch (IOException e) {
            throw new NodeError(e);
        }
    }

    public Issue fee(long fee) {
        this.fee = fee;
        return this;
    }

    @Override
    public long calcFee() {
        if (this.fee > 0) {
            return this.fee;
        } else {
            long totalFee = ONE_WAVES;
            totalFee += sender.isSmart() ? EXTRA_FEE : 0;
            return totalFee;
        }
    }

}
