package im.mak.paddle.params.readable;

import com.wavesplatform.transactions.common.AssetId;
import com.wavesplatform.transactions.common.Id;
import im.mak.paddle.Account;
import im.mak.paddle.params.LeaseCancelParams;

import java.util.List;

public class LeaseCancelParamsReadable extends LeaseCancelParams {

    public LeaseCancelParamsReadable(Account sender) {
        super(sender);
    }

    public Id getLeaseId() {
        return this.leaseId;
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
