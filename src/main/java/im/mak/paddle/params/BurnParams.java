package im.mak.paddle.params;

import com.wavesplatform.transactions.common.Amount;
import im.mak.paddle.Account;
import com.wavesplatform.transactions.BurnTransaction;
import com.wavesplatform.transactions.common.AssetId;

import static im.mak.paddle.Constants.EXTRA_FEE;
import static im.mak.paddle.Node.node;

public class BurnParams extends TxParams<BurnParams> {

    protected Amount amount;

    public BurnParams(Account sender) {
        super(sender, BurnTransaction.MIN_FEE);

        this.amount = Amount.of(0);
    }

    public BurnParams amount(Amount amount) {
        this.amount = amount;
        return this;
    }

    public BurnParams amount(long amount, AssetId assetId) {
        return amount(Amount.of(amount, assetId));
    }

    public Amount getAmount() {
        return this.amount;
    }

    @Override
    public long getFee() {
        long extraFee = node().getAssetDetails(amount.assetId() ).isScripted() ? EXTRA_FEE : 0;
        return super.getFee() + extraFee;
    }

}
