package im.mak.paddle.actions;

import com.wavesplatform.wavesj.DataEntry;
import com.wavesplatform.wavesj.transactions.DataTransaction;
import im.mak.paddle.Account;
import im.mak.paddle.actions.data.Entry;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static im.mak.paddle.Constants.EXTRA_FEE;
import static im.mak.paddle.Constants.MIN_FEE;

public class WriteData implements Action {

    public Account sender;
    public List<DataEntry<?>> data;
    public long fee;

    public WriteData(Account from) {
        this.sender = from;

        this.data = new LinkedList<>();
        this.fee = 0;
    }

    public static WriteData writeData(Account from) {
        return new WriteData(from);
    }

    public WriteData data(DataEntry<?>... data) {
        this.data = new LinkedList<>(Arrays.asList(data));
        return this;
    }

    public WriteData binary(String key, byte[] value) {
        data.add(Entry.binary(key, value));
        return this;
    }

    public WriteData bool(String key, boolean value) {
        data.add(Entry.bool(key, value));
        return this;
    }

    public WriteData integer(String key, long value) {
        data.add(Entry.integer(key, value));
        return this;
    }

    public WriteData string(String key, String value) {
        data.add(Entry.string(key, value));
        return this;
    }

    public WriteData fee(long fee) {
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

            byte[] bytes = new DataTransaction(sender.wavesAccount, data, 1, System.currentTimeMillis()).getBodyBytes();
            totalFee += ((bytes.length - 1) / 1024) * MIN_FEE;

            return totalFee;
        }
    }

}
