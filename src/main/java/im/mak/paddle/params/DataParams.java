package im.mak.paddle.params;

import im.mak.paddle.Account;
import com.wavesplatform.transactions.DataTransaction;
import com.wavesplatform.transactions.common.Base64String;
import com.wavesplatform.transactions.data.*;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class DataParams extends CommonParams<DataParams> {

    protected List<DataEntry> data;

    public DataParams(Account sender) {
        super(sender, DataTransaction.MIN_FEE);

        this.data = new LinkedList<>();
    }

    public DataParams binary(String key, Base64String value) {
        data.add(BinaryEntry.as(key, value));
        return this;
    }

    public DataParams binary(String key, byte[] value) {
        return binary(key, new Base64String(value));
    }

    public DataParams bool(String key, boolean value) {
        data.add(BooleanEntry.as(key, value));
        return this;
    }

    public DataParams integer(String key, long value) {
        data.add(IntegerEntry.as(key, value));
        return this;
    }

    public DataParams string(String key, String value) {
        data.add(StringEntry.as(key, value));
        return this;
    }

    public DataParams delete(String key) {
        data.add(DeleteEntry.as(key));
        return this;
    }

    public DataParams data(DataEntry... data) {
        this.data.addAll(Arrays.asList(data));
        return this;
    }

}
