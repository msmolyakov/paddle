package im.mak.paddle.actions;

import im.mak.paddle.Account;
import im.mak.waves.transactions.MassTransferTransaction;
import im.mak.waves.transactions.common.AssetId;
import im.mak.waves.transactions.common.Base58String;
import im.mak.waves.transactions.common.Recipient;
import im.mak.waves.transactions.mass.Transfer;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static im.mak.paddle.Constants.EXTRA_FEE;
import static im.mak.paddle.Constants.MIN_FEE;
import static im.mak.paddle.Node.node;

public class MassTransfer extends Action<MassTransfer> {

    public AssetId assetId;
    public List<Transfer> transfers;
    public Base58String attachment;

    public MassTransfer(Account sender) {
        super(sender, MassTransferTransaction.MIN_FEE);

        this.transfers = new LinkedList<>();
        this.assetId = AssetId.WAVES;
        this.attachment = Base58String.empty();
    }

    public MassTransfer assetId(AssetId assetId) {
        this.assetId = assetId;
        return this;
    }

    public MassTransfer to(Recipient recipient, long amount) {
        this.transfers.add(Transfer.to(recipient, amount));
        return this;
    }

    public MassTransfer to(Account recipient, long amount) {
        return to(recipient.address(), amount);
    }

    public MassTransfer transfers(Transfer... transfers) {
        this.transfers.addAll(Arrays.asList(transfers));
        return this;
    }

    public MassTransfer attachment(Base58String message) {
        this.attachment = message;
        return this;
    }

    public MassTransfer attachment(String message) {
        return attachment(new Base58String(message.getBytes(StandardCharsets.UTF_8)));
    }

    @Override
    public long calcFee() {
        if (feeAmount > 0)
            return feeAmount;

        long totalWavesFee = super.calcFee();
        if (!assetId.isWaves() && node().getAssetDetails(assetId).isScripted())
            totalWavesFee += EXTRA_FEE;

        totalWavesFee += ((transfers.size() + 1) / 2) * MIN_FEE;
        return totalWavesFee;
    }

}
