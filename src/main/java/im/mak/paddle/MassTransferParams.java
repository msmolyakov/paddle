package im.mak.paddle;

import com.wavesplatform.transactions.MassTransferTransaction;
import com.wavesplatform.transactions.common.AssetId;
import com.wavesplatform.transactions.common.Base58String;
import com.wavesplatform.transactions.common.Recipient;
import com.wavesplatform.transactions.mass.Transfer;
import im.mak.paddle.token.Token;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static im.mak.paddle.Node.node;
import static im.mak.paddle.util.Constants.EXTRA_FEE;
import static im.mak.paddle.util.Constants.MIN_FEE;

public class MassTransferParams extends CommonParams<MassTransferParams> {

    protected AssetId assetId;
    protected List<Transfer> transfers;
    protected Base58String attachment;

    protected MassTransferParams(Account sender) {
        super(sender, MassTransferTransaction.MIN_FEE);

        this.transfers = new LinkedList<>();
        this.assetId = AssetId.WAVES;
        this.attachment = Base58String.empty();
    }

    @Override
    protected long getFee() {
        long totalWavesFee = super.getFee();

        if (!assetId.isWaves() && node().getAssetDetails(assetId).isScripted())
            totalWavesFee += EXTRA_FEE;

        totalWavesFee += ((transfers.size() + 1) / 2) * MIN_FEE;

        return totalWavesFee;
    }

    public MassTransferParams assetId(AssetId assetId) {
        this.assetId = assetId;
        return this;
    }

    public MassTransferParams asset(Token token) {
        return assetId(token.id());
    }

    public MassTransferParams to(Recipient recipient, long amount) {
        this.transfers.add(Transfer.to(recipient, amount));
        return this;
    }

    public MassTransferParams to(Account recipient, long amount) {
        return to(recipient.address(), amount);
    }

    public MassTransferParams transfers(Transfer... transfers) {
        this.transfers.addAll(Arrays.asList(transfers));
        return this;
    }

    public MassTransferParams attachment(Base58String message) {
        this.attachment = message;
        return this;
    }

    public MassTransferParams attachmentUtf8(String utf8Message) {
        return attachment(new Base58String(utf8Message.getBytes(StandardCharsets.UTF_8)));
    }

}
