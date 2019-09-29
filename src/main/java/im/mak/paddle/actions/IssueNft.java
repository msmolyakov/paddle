package im.mak.paddle.actions;

import im.mak.paddle.Account;

import java.util.Random;

import static im.mak.paddle.Constants.EXTRA_FEE;
import static im.mak.paddle.Constants.MIN_FEE;
import static im.mak.paddle.Node.node;

public class IssueNft implements Action {

    public Account sender;
    public String name;
    public String description;
    public long quantity;
    public byte decimals;
    public boolean isReissuable;
    public String compiledScript;
    public long fee;

    public IssueNft(Account from) {
        this.sender = from;

        this.name = "NFT " + new Random().nextInt(1000);
        this.description = "";
        this.quantity = 1;
        this.decimals = 0;
        this.isReissuable = false;
        this.fee = 0;
    }

    public static IssueNft issueNft(Account from) {
        return new IssueNft(from);
    }

    public IssueNft name(String name) {
        this.name = name;
        return this;
    }

    public IssueNft description(String description) {
        this.description = description;
        return this;
    }

    public IssueNft script(String sourceCode) {
        this.compiledScript = sourceCode == null ? null : node().compileScript(sourceCode);
        return this;
    }

    public IssueNft fee(long fee) {
        this.fee = fee;
        return this;
    }

    @Override
    public long calcFee() {
        if (this.fee > 0) {
            return this.fee;
        } else {
            long totalFee = MIN_FEE;
            totalFee += sender.isSmart() ? EXTRA_FEE : 0;
            return totalFee;
        }
    }

}
