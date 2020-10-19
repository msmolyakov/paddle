package im.mak.paddle.actions;

import com.wavesplatform.wavesj.exceptions.NodeException;
import im.mak.paddle.Account;
import im.mak.waves.transactions.DataTransaction;
import im.mak.waves.transactions.common.Base64String;
import im.mak.waves.transactions.common.ByteString;
import im.mak.waves.transactions.data.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static im.mak.paddle.Constants.MIN_FEE;

public class WriteData extends Action<WriteData> {

    public List<DataEntry> data;

    public WriteData(Account sender) {
        super(sender, DataTransaction.MIN_FEE);

        this.data = new LinkedList<>();
    }

    public WriteData data(DataEntry... data) {
        this.data = new LinkedList<>(Arrays.asList(data));
        return this;
    }

    public WriteData binary(String key, Base64String value) {
        data.add(BinaryEntry.as(key, value));
        return this;
    }

    public WriteData binary(String key, byte[] value) {
        return binary(key, new Base64String(value));
    }

    public WriteData bool(String key, boolean value) {
        data.add(BooleanEntry.as(key, value));
        return this;
    }

    public WriteData integer(String key, long value) {
        data.add(IntegerEntry.as(key, value));
        return this;
    }

    public WriteData string(String key, String value) {
        data.add(StringEntry.as(key, value));
        return this;
    }

    public WriteData delete(String key) {
        data.add(DeleteEntry.as(key));
        return this;
    }

    @Override
    public long calcFee() {
        if (feeAmount > 0)
            return feeAmount;

        long totalWavesFee = super.calcFee();

        DataTransaction tx = DataTransaction.builder(data).sender(sender.publicKey()).getUnsigned();
        int payloadSize = tx.toProtobuf().getTransaction().getDataTransaction().getSerializedSize();
        totalWavesFee += ((payloadSize - 1) / 1024) * MIN_FEE;

        return totalWavesFee;
    }

}
