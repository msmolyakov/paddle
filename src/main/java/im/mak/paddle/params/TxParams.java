package im.mak.paddle.params;

import com.wavesplatform.transactions.common.AssetId;
import im.mak.paddle.Account;

public abstract class TxParams<T extends TxParams> {

    protected final Account sender;
    protected long timestamp;
    protected final long baseFee;
    protected AssetId feeAssetId;
    protected long additionalFee;
//    public List<Proof> proofs;
//    public List<PrivateKey> signers;

    protected TxParams(Account sender, long baseFee) {
        this.sender = sender;
        this.baseFee = baseFee;
//        this.proofs = new ArrayList<>();
//        this.signers = new ArrayList<>();
//todo        this.signers.add(sender.privateKey());
    }

    public T timestamp(long timestamp) {
        this.timestamp = timestamp;
        return (T) this;
    }

    public T additionalFee(long amount) {
        this.additionalFee = amount;
        return (T) this;
    }

    public Account getSender() {
        return this.sender;
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    public long getFee() {
        return baseFee + sender.getScriptInfo().extraFee() + additionalFee;
    }

    public AssetId getFeeAssetId() {
        return this.feeAssetId;
    }

}
