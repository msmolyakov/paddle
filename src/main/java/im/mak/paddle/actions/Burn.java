package im.mak.paddle.actions;

import im.mak.paddle.Account;

import static im.mak.paddle.Constants.EXTRA_FEE;
import static im.mak.paddle.Constants.MIN_FEE;

public class Burn implements Action {

    public Account sender;
    public String assetId;
    public long quantity;
    public long fee;

    public Burn(Account from) {
        this.sender = from;

        this.quantity = 0;
        this.fee = 0;
    }

    public static Burn burn(Account from) {
        return new Burn(from);
    }

    public Burn asset(String assetId) {
        this.assetId = assetId;
        return this;
    }

    public Burn quantity(long quantity) {
        this.quantity = quantity;
        return this;
    }

    public Burn fee(long fee) {
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
            totalFee += sender.node.isSmart(assetId) ? EXTRA_FEE : 0;
            return totalFee;
        }
    }

}
