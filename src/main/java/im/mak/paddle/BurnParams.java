package im.mak.paddle;

import com.wavesplatform.transactions.BurnTransaction;
import com.wavesplatform.transactions.common.AssetId;

import static im.mak.paddle.Node.node;
import static im.mak.paddle.util.Constants.EXTRA_FEE;

public class BurnParams extends CommonParams<BurnParams> {

    protected AssetId assetId;

    protected BurnParams(Account sender) {
        super(sender, BurnTransaction.MIN_FEE);

        this.assetId = AssetId.WAVES;
    }

    @Override
    protected long getFee() {
        long totalWavesFee = super.getFee();
        if (!assetId.isWaves() && node().getAssetDetails(assetId).isScripted())
            totalWavesFee += EXTRA_FEE;
        return totalWavesFee;
    }

    protected BurnParams assetId(AssetId assetId) {
        this.assetId = assetId;
        return this;
    }

}
