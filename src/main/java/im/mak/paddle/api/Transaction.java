package im.mak.paddle.api;

public class Transaction implements ITransaction {

    public byte type;
    public byte version;
    public String id;
    public String senderPublicKey;
    public String sender;
    public long timestamp;
    public long fee;
    public String[] proofs;

}
