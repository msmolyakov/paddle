package im.mak.paddle.params;

import com.wavesplatform.transactions.common.Amount;
import im.mak.paddle.Account;
import com.wavesplatform.transactions.BurnTransaction;
import com.wavesplatform.transactions.common.AssetId;
import im.mak.paddle.token.Asset;

public class BurnParams extends CommonParams<BurnParams> {

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

    public BurnParams amount(long amount, Asset asset) {
        return amount(amount, asset.id());
    }

}
