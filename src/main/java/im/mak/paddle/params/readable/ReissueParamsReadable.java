package im.mak.paddle.params.readable;

import com.wavesplatform.transactions.common.Amount;
import com.wavesplatform.transactions.common.AssetId;
import im.mak.paddle.Account;
import im.mak.paddle.params.ReissueParams;

import java.util.List;

import static im.mak.paddle.Node.node;
import static im.mak.paddle.util.Constants.EXTRA_FEE;

public class ReissueParamsReadable extends ReissueParams {

    public ReissueParamsReadable(Account sender) {
        super(sender);
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
