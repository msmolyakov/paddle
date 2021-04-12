package im.mak.paddle.params.readable;

import com.wavesplatform.transactions.common.AssetId;
import com.wavesplatform.transactions.common.Recipient;
import im.mak.paddle.Account;
import im.mak.paddle.params.LeaseParams;

import java.util.List;

public class LeaseParamsReadable extends LeaseParams {

    public LeaseParamsReadable(Account sender) {
        super(sender);
    }

    public Recipient getRecipient() {
        return this.recipient;
    }

    public long getAmount() {
        return this.amount;
    }

    /* COMMON PARAMS */

    @Override
    public Account getSender() {
        return super.getSender();
    }

    @Override
    public long getTimestamp() {
        return super.getTimestamp();
    }

    @Override
    public long getFee() {
        return super.getFee();
    }

    @Override
    public AssetId getFeeAssetId() {
        return super.getFeeAssetId();
    }

    @Override
    public List<Object> getSignersAndProofs() {
        return super.getSignersAndProofs();
    }

}
