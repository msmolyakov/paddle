package im.mak.paddle.actions;

import im.mak.paddle.Account;

import static im.mak.paddle.Constants.EXTRA_FEE;
import static im.mak.paddle.Constants.MIN_FEE;

public class Lease implements Action {

    public Account sender;
    public String recipient;
    public long amount;
    public long fee;

    public Lease(Account from) {
        this.sender = from;
        this.fee = 0;
    }

    public static Lease lease(Account from) {
        return new Lease(from);
    }

    public Lease to(String recipient) {
        this.recipient = recipient;
        return this;
    }

    public Lease to(Account account) {
        return to(account.address());
    }

    public Lease amount(long amount) {
        this.amount = amount;
        return this;
    }

    public Lease fee(long fee) {
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
