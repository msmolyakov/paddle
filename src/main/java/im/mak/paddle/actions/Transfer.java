package im.mak.paddle.actions;

import im.mak.paddle.Account;

import static im.mak.paddle.Constants.EXTRA_FEE;
import static im.mak.paddle.Constants.MIN_FEE;

public class Transfer implements Action {

    public Account sender;
    public String recipient;
    public long amount;
    public String assetId;
    public String attachment;
    public long fee;
    public String feeAssetId;

    public Transfer(Account from) {
        this.sender = from;
        this.recipient = this.sender.address();

        this.attachment = "";
        this.fee = 0;
        this.feeAssetId = "WAVES";
    }

    public static Transfer transfer(Account from) {
        return new Transfer(from);
    }

    public Transfer to(String recipient) {
        this.recipient = recipient;
        return this;
    }

    public Transfer to(Account account) {
        return to(account.address());
    }

    public Transfer amount(long amount) {
        this.amount = amount;
        return this;
    }

    public Transfer asset(String assetId) {
        this.assetId = assetId;
        return this;
    }

    public Transfer attachment(String message) {
        this.attachment = message;
        return this;
    }

    public Transfer fee(long fee) {
        this.fee = fee;
        return this;
    }

    public Transfer feeAsset(String assetId) {
        this.feeAssetId = assetId;
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
