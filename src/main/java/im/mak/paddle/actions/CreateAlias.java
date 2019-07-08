package im.mak.paddle.actions;

import im.mak.paddle.Account;

import static im.mak.paddle.Constants.EXTRA_FEE;
import static im.mak.paddle.Constants.MIN_FEE;

public class CreateAlias implements Action {

    public Account sender;
    public String alias;
    public long fee;

    public CreateAlias(Account from) {
        this.sender = from;
        this.fee = 0;
    }

    public static CreateAlias createAlias(Account sender) {
        return new CreateAlias(sender);
    }

    public CreateAlias alias(String alias) {
        this.alias = alias;
        return this;
    }

    public CreateAlias fee(long fee) {
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
