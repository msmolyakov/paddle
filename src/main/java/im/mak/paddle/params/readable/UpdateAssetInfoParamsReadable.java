package im.mak.paddle.params.readable;

import com.wavesplatform.transactions.common.AssetId;
import im.mak.paddle.Account;
import im.mak.paddle.params.UpdateAssetInfoParams;

import java.util.List;

import static im.mak.paddle.Node.node;
import static im.mak.paddle.util.Constants.EXTRA_FEE;

public class UpdateAssetInfoParamsReadable extends UpdateAssetInfoParams {

    public UpdateAssetInfoParamsReadable(Account sender) {
        super(sender);
    }

    public AssetId getAssetId() {
        return this.assetId;
    }

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    @Override
    public long getFee() {
        long totalWavesFee = super.getFee();
        totalWavesFee += node().getAssetDetails(assetId).isScripted() ? EXTRA_FEE : 0;
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
