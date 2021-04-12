package im.mak.paddle.params.readable;

import com.wavesplatform.transactions.common.Amount;
import com.wavesplatform.transactions.common.AssetId;
import im.mak.paddle.Account;
import im.mak.paddle.params.BurnParams;

import java.util.List;

import static im.mak.paddle.Node.node;
import static im.mak.paddle.util.Constants.EXTRA_FEE;

public class BurnParamsReadable extends BurnParams {

    public BurnParamsReadable(Account sender) {
        super(sender);
    }

    public Amount getAmount() {
        return this.amount;
    }

    @Override
    public long getFee() {
        long extraFee = node().getAssetDetails(amount.assetId()).isScripted() ? EXTRA_FEE : 0;
        return super.getFee() + extraFee;
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
