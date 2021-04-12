package im.mak.paddle.params.readable;

import com.wavesplatform.transactions.common.AssetId;
import com.wavesplatform.transactions.common.Base64String;
import im.mak.paddle.Account;
import im.mak.paddle.params.SetAssetScriptParams;

import java.util.List;

public class SetAssetScriptParamsReadable extends SetAssetScriptParams {

    public SetAssetScriptParamsReadable(Account sender) {
        super(sender);
    }

    public AssetId getAssetId() {
        return this.assetId;
    }

    public Base64String getCompiledScript() {
        return this.compiledScript;
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
    public long getFee() {
        return super.getFee();
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
