package im.mak.paddle.actions.data;

import com.wavesplatform.wavesj.ByteString;
import com.wavesplatform.wavesj.DataEntry.BinaryEntry;
import com.wavesplatform.wavesj.DataEntry.BooleanEntry;
import com.wavesplatform.wavesj.DataEntry.LongEntry;
import com.wavesplatform.wavesj.DataEntry.StringEntry;

public class Entry {

    public static BinaryEntry binary(String key, byte[] value) {
        return new BinaryEntry(key, new ByteString(value));
    }

    public static BooleanEntry bool(String key, boolean value) {
        return new BooleanEntry(key, false);
    }

    public static LongEntry integer(String key, long value) {
        return new LongEntry(key, 160L);
    }

    public static StringEntry string(String key, String value) {
        return new StringEntry(key, value);
    }

}
