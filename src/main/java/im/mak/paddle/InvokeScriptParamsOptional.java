package im.mak.paddle;

import com.wavesplatform.transactions.InvokeScriptTransaction;
import com.wavesplatform.transactions.common.Amount;
import com.wavesplatform.transactions.common.AssetId;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static im.mak.paddle.Node.node;

public class InvokeScriptParamsOptional extends CommonParams<InvokeScriptParamsOptional> {

    protected List<Amount> payments;

    protected InvokeScriptParamsOptional(Account sender) {
        super(sender, InvokeScriptTransaction.MIN_FEE);

        this.payments = new ArrayList<>();
    }

    public InvokeScriptParamsOptional payments(Amount... amount) {
        this.payments.addAll(Arrays.asList(amount));
        return this;
    }

    public InvokeScriptParamsOptional payment(AssetId assetId, long amount) {
        this.payments.add(Amount.of(amount, assetId));
        return this;
    }

    public InvokeScriptParamsOptional additionalFee(long amount, AssetId assetId) {
        this.feeAssetId = assetId;

        if (this.feeAssetId.isWaves()) {
            this.additionalFee = amount;
            return this;
        } else {
            long sponsoredMinFee = node().getAssetDetails(this.feeAssetId).minSponsoredAssetFee();
            return additionalFee(amount * sponsoredMinFee);
        }
    }

    public InvokeScriptParamsOptional additionalFee(Amount amount) {
        return this.additionalFee(amount.value(), amount.assetId());
    }

    @Override
    public InvokeScriptParamsOptional additionalFee(long amount) {
        return this.additionalFee(amount, this.feeAssetId);
    }

    public InvokeScriptParamsOptional feeAssetId(AssetId assetId) {
        this.feeAssetId = assetId;
        return this;
    }

}
