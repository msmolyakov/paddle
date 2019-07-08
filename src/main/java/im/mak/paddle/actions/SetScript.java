package im.mak.paddle.actions;

import im.mak.paddle.Account;
import im.mak.paddle.exceptions.NodeError;

import java.io.IOException;
import java.nio.file.Path;

import static im.mak.paddle.Constants.EXTRA_FEE;
import static im.mak.paddle.Constants.MIN_FEE;
import static java.nio.file.Files.readAllLines;

public class SetScript implements Action {

    public Account sender;
    public String compiledScript;
    public long fee;

    public SetScript(Account from) {
        this.sender = from;
        this.fee = 0;
    }

    public static SetScript setScript(Account from) {
        return new SetScript(from);
    }

    public SetScript script(String sourceCode) {
        this.compiledScript = sourceCode == null ? null : this.sender.node.compileScript(sourceCode);
        return this;
    }

    public SetScript script(Path filePath) {
        try {
            return script(String.join("\n", readAllLines(filePath)));
        } catch (IOException e) {
            throw new NodeError(e);
        }
    }

    public SetScript fee(long fee) {
        this.fee = fee;
        return this;
    }

    @Override
    public long calcFee() {
        if (this.fee > 0) {
            return this.fee;
        } else {
            long totalFee = MIN_FEE * 10;
            totalFee += sender.isSmart() ? EXTRA_FEE : 0;
            return totalFee;
        }
    }

}
