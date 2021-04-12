package im.mak.paddle.params.readable;

import com.wavesplatform.transactions.common.AssetId;
import im.mak.paddle.Account;
import im.mak.paddle.params.SponsorFeeParams;

import java.util.List;

public class SponsorFeeParamsReadable extends SponsorFeeParams {

    public SponsorFeeParamsReadable(Account sender) {
        super(sender);
    }

    public AssetId getAssetId() {
        return this.assetId;
    }

    public long getMinSponsoredFee() {
        return this.minSponsoredAssetFee;
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
