package im.mak.paddle.actions;

import com.wavesplatform.wavesj.exceptions.NodeException;
import im.mak.paddle.Account;
import im.mak.waves.transactions.SetAssetScriptTransaction;
import im.mak.waves.transactions.common.AssetId;
import im.mak.waves.transactions.common.Base64String;

import java.io.IOException;

import static im.mak.paddle.Constants.EXTRA_FEE;
import static im.mak.paddle.Constants.ONE_WAVES;
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
