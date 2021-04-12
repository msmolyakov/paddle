package im.mak.paddle.params.readable;

import com.wavesplatform.transactions.common.AssetId;
import com.wavesplatform.transactions.common.Base64String;
import im.mak.paddle.Account;
import im.mak.paddle.params.SetScriptParams;

import java.util.List;

public class SetScriptParamsReadable extends SetScriptParams {

    public SetScriptParamsReadable(Account sender) {
        super(sender);
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
