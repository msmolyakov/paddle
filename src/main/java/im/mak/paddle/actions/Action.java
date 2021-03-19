package im.mak.paddle.actions;

import im.mak.paddle.Account;
import com.wavesplatform.transactions.account.PrivateKey;
import com.wavesplatform.transactions.common.Proof;

import java.util.ArrayList;
import java.util.List;

public abstract class Action<T extends Action> { //TODO rename

    //TODO UpdateAssetInfo action

    public final Account sender;
    public long timestamp;
    public long feeAmount;
//    public List<Proof> proofs;
//    public List<PrivateKey> signers;

    //TODO extraFee (additional)
    protected final long baseFee;

    protected Action(Account sender, long baseFee) {
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

    public T fee(long amount) {
        this.feeAmount = amount;
        return (T) this;
    }

    public long calcFee() {
        if (feeAmount > 0)
            return feeAmount;
        return baseFee + sender.getScriptInfo().extraFee();
    }

}
