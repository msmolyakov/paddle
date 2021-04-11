package im.mak.paddle.params;

import im.mak.paddle.Account;
import com.wavesplatform.transactions.SetAssetScriptTransaction;
import com.wavesplatform.transactions.common.AssetId;
import com.wavesplatform.transactions.common.Base64String;
import im.mak.paddle.token.Asset;

import static im.mak.paddle.Node.node;

public class SetAssetScriptParams extends TxParams<SetAssetScriptParams> {

    protected AssetId assetId;
    protected Base64String compiledScript;

    public SetAssetScriptParams(Account sender) {
        super(sender, SetAssetScriptTransaction.MIN_FEE);
    }

    public SetAssetScriptParams assetId(AssetId assetId) {
        this.assetId = assetId;
        return this;
    }

    public SetAssetScriptParams asset(Asset asset) {
        return assetId(asset.id());
    }

    public SetAssetScriptParams compiledScript(Base64String compiled) {
        this.compiledScript = compiled;
        return this;
    }

    public SetAssetScriptParams script(String sourceCode) {
        return compiledScript(sourceCode == null ? null : node().compileScript(sourceCode).script());
    }

    public AssetId getAssetId() {
        return this.assetId;
    }

    public Base64String getCompiledScript() {
        return this.compiledScript;
    }

}
