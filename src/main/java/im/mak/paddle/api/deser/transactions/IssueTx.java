package im.mak.paddle.api.deser.transactions;

import java.util.List;

public class IssueTx {

    public int type;
    public int version;
    public byte chainId;
    public String id;
    public String senderPublicKey;
    public String sender;
    public String name;
    public String description;
    public long quantity;
    public int decimals;
    public boolean reissuable;
    public String script;
    public long fee;
    public String feeAssetId;
    public long timestamp;
    public List<String> proofs;

}
