package im.mak.paddle;

import com.wavesplatform.transactions.UpdateAssetInfoTransaction;
import com.wavesplatform.transactions.common.AssetId;

import static im.mak.paddle.Node.node;
import static im.mak.paddle.util.Constants.EXTRA_FEE;

public class UpdateAssetInfoParams extends CommonParams<UpdateAssetInfoParams> {

    protected AssetId assetId;

    protected UpdateAssetInfoParams(Account sender) {
        super(sender, UpdateAssetInfoTransaction.MIN_FEE);
    }

    @Override
    protected long getFee() {
        long totalWavesFee = super.getFee();
        if (!assetId.isWaves() && node().getAssetDetails(assetId).isScripted())
            totalWavesFee += EXTRA_FEE;
        return totalWavesFee;
    }

    protected UpdateAssetInfoParams assetId(AssetId assetId) {
        this.assetId = assetId;
        return this;
    }

}
