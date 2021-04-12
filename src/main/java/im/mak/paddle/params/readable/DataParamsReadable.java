package im.mak.paddle.params.readable;

import com.wavesplatform.transactions.DataTransaction;
import com.wavesplatform.transactions.common.AssetId;
import com.wavesplatform.transactions.data.DataEntry;
import im.mak.paddle.Account;
import im.mak.paddle.params.DataParams;

import java.util.List;

import static im.mak.paddle.util.Constants.MIN_FEE;

public class DataParamsReadable extends DataParams {

    public DataParamsReadable(Account sender) {
        super(sender);
    }

    public List<DataEntry> getData() {
        return this.data;
    }

    @Override
    public long getFee() {
        long totalWavesFee = super.getFee();

        //calculation only by protobuf bytes, because latest version of DataTransaction is used by default
        DataTransaction tx = DataTransaction.builder(data).sender(sender.publicKey()).getUnsigned();
        int payloadSize = tx.toProtobuf().getTransaction().getDataTransaction().getSerializedSize();
        totalWavesFee += ((payloadSize - 1) / 1024) * MIN_FEE;

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
