package im.mak.paddle.params;

import com.wavesplatform.transactions.common.Amount;
import im.mak.paddle.Account;
import com.wavesplatform.transactions.ReissueTransaction;
import com.wavesplatform.transactions.common.AssetId;
import im.mak.paddle.token.Asset;

public class ReissueParams extends CommonParams<ReissueParams> {

    protected Amount amount;
    protected boolean reissuable;

    public ReissueParams(Account sender) {
        super(sender, ReissueTransaction.MIN_FEE);

        this.amount = Amount.of(0);
        this.reissuable = true;
    }

    public ReissueParams amount(Amount amount) {
        this.amount = amount;
        return this;
    }

    public ReissueParams amount(long amount, AssetId assetId) {
        return amount(Amount.of(amount, assetId));
    }

    public ReissueParams amount(long amount, Asset asset) {
        return amount(amount, asset.id());
    }

    public ReissueParams reissuable(boolean reissuable) {
        this.reissuable = reissuable;
        return this;
    }

}
