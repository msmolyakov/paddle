package im.mak.paddle.params.readable;

import com.wavesplatform.transactions.common.Alias;
import com.wavesplatform.transactions.common.AssetId;
import im.mak.paddle.Account;
import im.mak.paddle.params.CreateAliasParams;

import java.util.List;

public class CreateAliasParamsReadable extends CreateAliasParams {

    public CreateAliasParamsReadable(Account sender) {
        super(sender);
    }

    public Alias getAlias() {
        return this.alias;
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
