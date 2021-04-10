package im.mak.paddle.params;

import im.mak.paddle.Account;
import com.wavesplatform.transactions.TransferTransaction;
import com.wavesplatform.transactions.common.Amount;
import com.wavesplatform.transactions.common.AssetId;
import com.wavesplatform.transactions.common.Base58String;
import com.wavesplatform.transactions.common.Recipient;

import java.nio.charset.StandardCharsets;

import static im.mak.paddle.Constants.EXTRA_FEE;
import static im.mak.paddle.Constants.MIN_FEE;
import static im.mak.paddle.Node.node;

public class TransferParams extends TxParams<TransferParams> {

    protected Recipient recipient;
    protected Amount amount;
    protected Base58String attachment;

    public TransferParams(Account sender) {
        super(sender, TransferTransaction.MIN_FEE);

        this.recipient = this.sender.address();
        this.amount = Amount.of(0);
        this.attachment = Base58String.empty();
        this.feeAssetId = AssetId.WAVES;
    }

    public TransferParams to(Recipient recipient) {
        this.recipient = recipient;
        return this;
    }

    public TransferParams to(Account account) {
        return to(account.address());
    }

    public TransferParams amount(Amount amount) {
        this.amount = amount;
        return this;
    }

    public TransferParams amount(long amount, AssetId assetId) {
        return amount(Amount.of(amount, assetId));
    }

    public TransferParams amount(long amount) {
        return amount(amount, AssetId.WAVES);
    }

    public TransferParams attachment(Base58String message) {
        this.attachment = message;
        return this;
    }

    public TransferParams attachmentUtf8(String utf8Message) {
        return attachment(new Base58String(utf8Message.getBytes(StandardCharsets.UTF_8)));
    }

    public TransferParams additionalFee(long amount, AssetId assetId) {
        this.feeAssetId = assetId;

        if (this.feeAssetId.isWaves()) {
            this.additionalFee = amount;
            return this;
        } else {
            long sponsoredMinFee = node().getAssetDetails(this.feeAssetId).minSponsoredAssetFee();
            return additionalFee(amount * sponsoredMinFee);
        }
    }

    public TransferParams additionalFee(Amount amount) {
        return this.additionalFee(amount.value(), amount.assetId());
    }

    @Override
    public TransferParams additionalFee(long amount) {
        return this.additionalFee(amount, this.feeAssetId);
    }

    public TransferParams feeAssetId(AssetId assetId) {
        this.feeAssetId = assetId;
        return this;
    }

    public Recipient getRecipient() {
        return this.recipient;
    }

    public Amount getAmount() {
        return this.amount;
    }

    public Base58String getAttachment() {
        return this.attachment;
    }

    @Override
    public long getFee() {
        long totalWavesFee = super.getFee();

        if (!amount.assetId().isWaves() && node().getAssetDetails(amount.assetId()).isScripted())
            totalWavesFee += EXTRA_FEE;

        if (feeAssetId.isWaves())
            return totalWavesFee;
        else {
            long minSponsoredFee = node().getAssetDetails(feeAssetId).minSponsoredAssetFee();
            long increment = totalWavesFee % MIN_FEE == 0 ? 0 : minSponsoredFee;
            return (totalWavesFee / MIN_FEE) * minSponsoredFee + increment;
        }
    }

}