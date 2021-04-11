package im.mak.paddle.params;

import im.mak.paddle.Account;
import com.wavesplatform.transactions.SponsorFeeTransaction;
import com.wavesplatform.transactions.common.AssetId;
import im.mak.paddle.token.Asset;

//TODO action to cancel sponsorship

public class SponsorFeeParams extends TxParams<SponsorFeeParams> {

    protected AssetId assetId;
    protected long minSponsoredAssetFee;

    public SponsorFeeParams(Account sender) {
        super(sender, SponsorFeeTransaction.MIN_FEE);

        this.minSponsoredAssetFee = 1;
    }

    public SponsorFeeParams assetId(AssetId assetId) {
        this.assetId = assetId;
        return this;
    }

    public SponsorFeeParams asset(Asset asset) {
        return assetId(asset.id());
    }

    public SponsorFeeParams amountForMinFee(long assetAmount) {
        this.minSponsoredAssetFee = assetAmount;
        return this;
    }

    public AssetId getAssetId() {
        return this.assetId;
    }

    public long getMinSponsoredFee() {
        return this.minSponsoredAssetFee;
    }

}
