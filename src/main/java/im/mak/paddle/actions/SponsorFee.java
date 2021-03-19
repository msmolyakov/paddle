package im.mak.paddle.actions;

import im.mak.paddle.Account;
import com.wavesplatform.transactions.SponsorFeeTransaction;
import com.wavesplatform.transactions.common.AssetId;

//TODO action to cancel sponsorship

public class SponsorFee extends Action<SponsorFee> {

    public AssetId assetId;
    public long minSponsoredAssetFee;

    public SponsorFee(Account sender) {
        super(sender, SponsorFeeTransaction.MIN_FEE);

        this.minSponsoredAssetFee = 1;
    }

    public SponsorFee assetId(AssetId assetId) {
        this.assetId = assetId;
        return this;
    }

    public SponsorFee amountForMinFee(long assetAmount) {
        this.minSponsoredAssetFee = assetAmount;
        return this;
    }

}
