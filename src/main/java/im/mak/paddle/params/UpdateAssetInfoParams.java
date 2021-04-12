package im.mak.paddle.params;

import com.wavesplatform.transactions.UpdateAssetInfoTransaction;
import com.wavesplatform.transactions.common.AssetId;
import im.mak.paddle.Account;
import im.mak.paddle.token.Asset;

public class UpdateAssetInfoParams extends CommonParams<UpdateAssetInfoParams> {

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

}
