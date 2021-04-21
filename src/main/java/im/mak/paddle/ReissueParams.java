package im.mak.paddle;

import com.wavesplatform.transactions.ReissueTransaction;
import com.wavesplatform.transactions.common.AssetId;

import static im.mak.paddle.Node.node;
import static im.mak.paddle.util.Constants.EXTRA_FEE;

public class ReissueParams extends CommonParams<ReissueParams> {

    protected AssetId assetId;
    protected boolean reissuable;

    protected ReissueParams(Account sender) {
        super(sender, ReissueTransaction.MIN_FEE);

        this.reissuable = true;
    }

    @Override
    protected long getFee() {
        long totalWavesFee = super.getFee();
        if (!assetId.isWaves() && node().getAssetDetails(assetId).isScripted())
            totalWavesFee += EXTRA_FEE;
        return totalWavesFee;
    }

    protected ReissueParams assetId(AssetId assetId) {
        this.assetId = assetId;
        return this;
    }

    public ReissueParams reissuable(boolean reissuable) {
        this.reissuable = reissuable;
        return this;
    }

}
