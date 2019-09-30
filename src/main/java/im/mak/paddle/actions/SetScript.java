package im.mak.paddle.actions;

import im.mak.paddle.Account;

import static im.mak.paddle.Constants.EXTRA_FEE;
import static im.mak.paddle.Constants.MIN_FEE;
import static im.mak.paddle.Node.node;

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
        this.compiledScript = sourceCode == null ? null : node().compileScript(sourceCode);
        return this;
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
