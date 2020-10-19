package im.mak.paddle.actions;

import com.wavesplatform.wavesj.exceptions.NodeException;
import im.mak.paddle.Account;
import im.mak.waves.transactions.TransferTransaction;
import im.mak.waves.transactions.common.Amount;
import im.mak.waves.transactions.common.AssetId;
import im.mak.waves.transactions.common.Base58String;
import im.mak.waves.transactions.common.Recipient;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static im.mak.paddle.Constants.EXTRA_FEE;
import static im.mak.paddle.Constants.MIN_FEE;
import static im.mak.paddle.Node.node;

public class Transfer extends Action<Transfer> {

    public Recipient recipient;
    public Amount amount;
    public Base58String attachment;
    public AssetId feeAssetId;

    public Transfer(Account sender) {
        super(sender, TransferTransaction.MIN_FEE);

        this.recipient = this.sender.address(); //TODO bullshit
        this.attachment = Base58String.empty();
        this.feeAssetId = AssetId.WAVES;
    }

    public Transfer to(Recipient recipient) {
        this.recipient = recipient;
        return this;
    }

    public Transfer to(Account account) {
        return to(account.address());
    }

    public Transfer amount(Amount amount) {
        this.amount = amount;
        return this;
    }

    public Transfer amount(long amount, AssetId assetId) {
        return amount(Amount.of(amount, assetId));
    }

    public Transfer amount(long amount) {
        return amount(amount, AssetId.WAVES);
    }

    public Transfer attachment(Base58String attachment) {
        this.attachment = attachment;
        return this;
    }

    public Transfer attachment(String message) {
        return attachment(new Base58String(message.getBytes(StandardCharsets.UTF_8)));
    }

    public Transfer fee(long amount, AssetId assetId) {
        this.feeAmount = amount;
        this.feeAssetId = assetId;
        return this;
    }

    public Transfer fee(Amount fee) {
        return fee(fee.value(), fee.assetId());
    }

    public Transfer fee(AssetId assetId) {
        this.feeAssetId = assetId;
        return this;
    }

    @Override
    public long calcFee() {
        if (feeAmount > 0)
            return feeAmount;

        long totalWavesFee = super.calcFee();
        if (!amount.assetId().isWaves() && node().getAssetDetails(amount.assetId()).isScripted())
            totalWavesFee += EXTRA_FEE;

        if (feeAssetId.isWaves())
            return totalWavesFee;

        long sponsoredMinAssetFee = node().getAssetDetails(feeAssetId).minSponsoredAssetFee();
        return sponsoredMinAssetFee * (long) Math.ceil((double) totalWavesFee / MIN_FEE);
    }

}
