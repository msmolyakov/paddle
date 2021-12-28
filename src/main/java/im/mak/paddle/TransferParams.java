package im.mak.paddle;

import com.wavesplatform.transactions.TransferTransaction;
import com.wavesplatform.transactions.common.Amount;
import com.wavesplatform.transactions.common.AssetId;
import com.wavesplatform.transactions.common.Base58String;

import java.nio.charset.StandardCharsets;

import static im.mak.paddle.Node.node;
import static im.mak.paddle.util.Constants.EXTRA_FEE;
import static im.mak.paddle.util.Constants.MIN_FEE;

public class TransferParams extends CommonParams<TransferParams> {

    protected AssetId assetId;
    protected Base58String attachment;

    protected TransferParams(Account sender) {
        super(sender, TransferTransaction.MIN_FEE);

        this.assetId = AssetId.WAVES;
        this.attachment = Base58String.empty();
        this.feeAssetId = AssetId.WAVES;
    }

    @Override
    protected long getFee() {
        long totalWavesFee = super.getFee();

        if (!assetId.isWaves() && node().getAssetDetails(assetId).isScripted())
            totalWavesFee += EXTRA_FEE;

        if (feeAssetId.isWaves())
            return totalWavesFee;
        else {
            long minSponsoredFee = node().getAssetDetails(feeAssetId).minSponsoredAssetFee();
            long increment = totalWavesFee % MIN_FEE == 0 ? 0 : minSponsoredFee;
            return (totalWavesFee / MIN_FEE) * minSponsoredFee + increment;
        }
    }

    protected TransferParams assetId(AssetId assetId) {
        this.assetId = assetId;
        return this;
    }

    public TransferParams attachment(Base58String message) {
        this.attachment = message;
        return this;
    }

    public TransferParams attachmentUtf8(String utf8Message) {
        return attachment(new Base58String(utf8Message.getBytes(StandardCharsets.UTF_8)));
    }

    public TransferParams additionalFee(long amount, AssetId assetId) {
        this.feeAssetId = assetId;

        if (this.feeAssetId.isWaves()) {
            this.additionalFee = amount;
        } else {
            long sponsoredMinFee = node().getAssetDetails(this.feeAssetId).minSponsoredAssetFee();
            this.additionalFee = amount * sponsoredMinFee;
        }

        return this;
    }

    public TransferParams additionalFee(Amount amount) {
        return this.additionalFee(amount.value(), amount.assetId());
    }

    @Override
    public TransferParams additionalFee(long amount) {
        return this.additionalFee(amount, this.feeAssetId);
    }

    public TransferParams feeAssetId(AssetId assetId) {
        this.feeAssetId = assetId;
        return this;
    }

}
