package im.mak.paddle;

import com.wavesplatform.transactions.ExchangeTransaction;
import com.wavesplatform.transactions.common.AssetId;
import com.wavesplatform.transactions.exchange.AssetPair;

import static im.mak.paddle.Node.node;
import static im.mak.paddle.util.Constants.EXTRA_FEE;

public class ExchangeParams extends CommonParams<ExchangeParams> {

    protected AssetId amountAssetId;
    protected AssetId priceAssetId;
    protected long buyMatcherFee;
    protected long sellMatcherFee;

    protected ExchangeParams(Account sender) {
        super(sender, ExchangeTransaction.MIN_FEE);

        this.buyMatcherFee = ExchangeTransaction.MIN_FEE;
        this.sellMatcherFee = ExchangeTransaction.MIN_FEE;
    }

    @Override
    protected long getFee() {
        long totalWavesFee = super.getFee();

        //TODO what about auto calc fee for orders?
        if (!amountAssetId.isWaves() && node().getAssetDetails(amountAssetId).isScripted())
            totalWavesFee += EXTRA_FEE;
        if (!priceAssetId.isWaves() && node().getAssetDetails(priceAssetId).isScripted())
            totalWavesFee += EXTRA_FEE;

        return totalWavesFee;
    }

    protected ExchangeParams assetPair(AssetPair assetPair) {
        this.amountAssetId = assetPair.left();
        this.priceAssetId = assetPair.right();
        return this;
    }

    public ExchangeParams buyMatcherFee(long value) {
        this.buyMatcherFee = value;
        return this;
    }

    public ExchangeParams sellMatcherFee(long value) {
        this.sellMatcherFee = value;
        return this;
    }

}
