package im.mak.paddle.api.deser;

import com.wavesplatform.wavesj.Base64;

public class DataEntry {

    public String key;
    public String type;
    public Object value; //TODO several types!

    public byte[] asBinary() { //TODO java.lang.String cannot be cast to [B
        return Base64.decode((String) value);
    }

    public boolean asBoolean() {
        return (boolean) value;
    }

    public long asInteger() {
        return (long) value;
    }

    public String asString() {
        return (String) value;
    }

}
