package im.mak.paddle.actions;

import im.mak.paddle.Account;
import com.wavesplatform.transactions.SetAssetScriptTransaction;
import com.wavesplatform.transactions.common.AssetId;
import com.wavesplatform.transactions.common.Base64String;

import static im.mak.paddle.Node.node;

public class SetAssetScript extends Action<SetAssetScript> {

    public AssetId assetId;
    public Base64String compiledScript;

    public SetAssetScript(Account sender) {
        super(sender, SetAssetScriptTransaction.MIN_FEE);
    }

    public SetAssetScript assetId(AssetId assetId) {
        this.assetId = assetId;
        return this;
    }

    public SetAssetScript compiledScript(Base64String compiled) {
        this.compiledScript = compiled;
        return this;
    }

    public SetAssetScript script(String sourceCode) {
        return compiledScript(node().compileScript(sourceCode).script());
    }

}
