package im.mak.paddle.params.readable;

import com.wavesplatform.transactions.common.AssetId;
import com.wavesplatform.transactions.common.Base58String;
import com.wavesplatform.transactions.mass.Transfer;
import im.mak.paddle.Account;
import im.mak.paddle.params.MassTransferParams;

import java.util.List;

import static im.mak.paddle.Node.node;
import static im.mak.paddle.util.Constants.EXTRA_FEE;
import static im.mak.paddle.util.Constants.MIN_FEE;

public class MassTransferParamsReadable extends MassTransferParams {

    public MassTransferParamsReadable(Account sender) {
        super(sender);
    }

    public AssetId getAssetId() {
        return this.assetId;
    }

    public List<Transfer> getTransfers() {
        return this.transfers;
    }

    public Base58String getAttachment() {
        return this.attachment;
    }

    @Override
    public long getFee() {
        long totalWavesFee = super.getFee();

        if (!assetId.isWaves() && node().getAssetDetails(assetId).isScripted())
            totalWavesFee += EXTRA_FEE;

        totalWavesFee += ((transfers.size() + 1) / 2) * MIN_FEE;

        return totalWavesFee;
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
