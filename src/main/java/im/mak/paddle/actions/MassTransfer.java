package im.mak.paddle.actions;

import im.mak.paddle.Account;
import im.mak.paddle.actions.mass.Recipient;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static im.mak.paddle.Constants.EXTRA_FEE;
import static im.mak.paddle.Constants.MIN_FEE;

public class MassTransfer implements Action {

    public Account sender;
    public String assetId;
    public List<Recipient> transfers;
    public String attachment;
    public long fee;

    public MassTransfer(Account from) {
        this.sender = from;

        this.transfers = new LinkedList<>();
        this.attachment = "";
        this.fee = 0;
    }

    public static MassTransfer massTransfer(Account from) {
        return new MassTransfer(from);
    }

    public MassTransfer asset(String assetId) {
        this.assetId = assetId;
        return this;
    }

    public MassTransfer recipients(Recipient... transfers) {
        this.transfers = new LinkedList<>(Arrays.asList(transfers));
        return this;
    }

    public MassTransfer attachment(String message) {
        this.attachment = message;
        return this;
    }

    public MassTransfer fee(long fee) {
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
            totalFee += ((transfers.size() + 1) / 2) * MIN_FEE;
            return totalFee;
        }
    }

}
