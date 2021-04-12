package im.mak.paddle.params.readable;

import com.wavesplatform.transactions.common.AssetId;
import com.wavesplatform.transactions.common.Base64String;
import im.mak.paddle.Account;
import im.mak.paddle.params.IssueParams;

import java.util.List;

public class IssueParamsReadable extends IssueParams {

    public IssueParamsReadable(Account sender) {
        super(sender);
    }

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    public long getQuantity() {
        return this.quantity;
    }

    public int getDecimals() {
        return this.decimals;
    }

    public boolean isReissuable() {
        return this.reissuable;
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
