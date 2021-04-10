package im.mak.paddle.params;

import com.wavesplatform.transactions.common.Amount;
import im.mak.paddle.Account;
import com.wavesplatform.transactions.ReissueTransaction;
import com.wavesplatform.transactions.common.AssetId;

import static im.mak.paddle.Constants.EXTRA_FEE;
import static im.mak.paddle.Node.node;

public class ReissueParams extends TxParams<ReissueParams> {

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

    public ReissueParams reissuable(boolean reissuable) {
        this.reissuable = reissuable;
        return this;
    }

    public Amount getAmount() {
        return this.amount;
    }

    public boolean isReissuable() {
        return this.reissuable;
    }

    @Override
    public long getFee() {
        long totalWavesFee = super.getFee();
        totalWavesFee += node().getAssetDetails(amount.assetId()).isScripted() ? EXTRA_FEE : 0;
        return totalWavesFee;
    }

}
