package im.mak.paddle.actions;

import im.mak.paddle.Account;

import static im.mak.paddle.Constants.EXTRA_FEE;
import static im.mak.paddle.Constants.MIN_FEE;

public class LeaseCancel implements Action {

    public String leaseId;
    public Account sender;
    public long fee;

    public LeaseCancel(Account from) {
        this.sender = from;
        this.fee = 0;
    }

    public static LeaseCancel leaseCancel(Account from) {
        return new LeaseCancel(from);
    }

    public LeaseCancel leaseId(String leaseId) {
        this.leaseId = leaseId;
        return this;
    }

    public LeaseCancel fee(long fee) {
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
