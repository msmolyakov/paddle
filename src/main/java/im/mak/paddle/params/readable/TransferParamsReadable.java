package im.mak.paddle.params.readable;

import com.wavesplatform.transactions.common.Amount;
import com.wavesplatform.transactions.common.AssetId;
import com.wavesplatform.transactions.common.Base58String;
import com.wavesplatform.transactions.common.Recipient;
import im.mak.paddle.Account;
import im.mak.paddle.params.TransferParams;

import java.util.List;

import static im.mak.paddle.Node.node;
import static im.mak.paddle.util.Constants.EXTRA_FEE;
import static im.mak.paddle.util.Constants.MIN_FEE;

public class TransferParamsReadable extends TransferParams {

    public TransferParamsReadable(Account sender) {
        super(sender);
    }

    public Recipient getRecipient() {
        return this.recipient;
    }

    public Amount getAmount() {
        return this.amount;
    }

    public Base58String getAttachment() {
        return this.attachment;
    }

    @Override
    public long getFee() {
        long totalWavesFee = super.getFee();

        if (!amount.assetId().isWaves() && node().getAssetDetails(amount.assetId()).isScripted())
            totalWavesFee += EXTRA_FEE;

        if (feeAssetId.isWaves())
            return totalWavesFee;
        else {
            long minSponsoredFee = node().getAssetDetails(feeAssetId).minSponsoredAssetFee();
            long increment = totalWavesFee % MIN_FEE == 0 ? 0 : minSponsoredFee;
            return (totalWavesFee / MIN_FEE) * minSponsoredFee + increment;
        }
    }

    /* COMMON PARAMS */

    @Override
    public Account getSender() {
        return super.getSender();
    }

    @Override
    public long getTimestamp() {
        return super.getTimestamp();
    }

    @Override
    public AssetId getFeeAssetId() {
        return super.getFeeAssetId();
    }

    @Override
    public List<Object> getSignersAndProofs() {
        return super.getSignersAndProofs();
    }

}
