package im.mak.paddle.actions;

import im.mak.paddle.Account;
import com.wavesplatform.transactions.ReissueTransaction;
import com.wavesplatform.transactions.common.AssetId;

import static im.mak.paddle.Constants.EXTRA_FEE;
import static im.mak.paddle.Node.node;

public class Reissue extends Action<Reissue> {

    public AssetId assetId;
    public long amount;
    public boolean reissuable;

    public Reissue(Account sender) {
        super(sender, ReissueTransaction.MIN_FEE);

        this.assetId = AssetId.WAVES;
        this.amount = 0;
        this.reissuable = true;
    }

    public Reissue assetId(AssetId assetId) {
        this.assetId = assetId;
        return this;
    }

    public Reissue amount(long amount) {
        this.amount = amount;
        return this;
    }

    public Reissue reissuable(boolean reissuable) {
        this.reissuable = reissuable;
        return this;
    }

    @Override
    public long calcFee() {
        if (feeAmount > 0)
            return feeAmount;

        long extraFee = node().getAssetDetails(assetId).isScripted() ? EXTRA_FEE : 0;
        return super.calcFee() + extraFee;
    }

}
