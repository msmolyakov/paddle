package im.mak.paddle.params;

import com.wavesplatform.transactions.UpdateAssetInfoTransaction;
import com.wavesplatform.transactions.common.AssetId;
import im.mak.paddle.Account;
import im.mak.paddle.token.Asset;

import static im.mak.paddle.util.Constants.EXTRA_FEE;
import static im.mak.paddle.Node.node;

public class UpdateAssetInfoParams extends TxParams<UpdateAssetInfoParams> {

    protected AssetId assetId;
    protected String name;
    protected String description;

    public UpdateAssetInfoParams(Account sender) {
        super(sender, UpdateAssetInfoTransaction.MIN_FEE);

        this.name = null;
        this.description = null;
    }

    public UpdateAssetInfoParams assetId(AssetId assetId) {
        this.assetId = assetId;
        return this;
    }

    public UpdateAssetInfoParams asset(Asset asset) {
        return assetId(asset.id());
    }

    public UpdateAssetInfoParams name(String name) {
        this.name = name;
        return this;
    }

    public UpdateAssetInfoParams description(String description) {
        this.description = description;
        return this;
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

}
