package im.mak.paddle.token;

import com.wavesplatform.transactions.InvokeScriptTransaction;
import com.wavesplatform.transactions.IssueTransaction;
import com.wavesplatform.transactions.UpdateAssetInfoTransaction;
import com.wavesplatform.transactions.account.Address;
import com.wavesplatform.transactions.account.PublicKey;
import com.wavesplatform.transactions.common.Amount;
import com.wavesplatform.transactions.common.AssetId;
import com.wavesplatform.transactions.common.Id;
import com.wavesplatform.transactions.common.Recipient;
import com.wavesplatform.wavesj.AssetDetails;
import com.wavesplatform.wavesj.AssetDistribution;
import com.wavesplatform.wavesj.StateChanges;
import com.wavesplatform.wavesj.TransactionInfo;
import com.wavesplatform.wavesj.actions.IssueAction;
import im.mak.paddle.util.RecipientResolver;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static im.mak.paddle.util.Constants.MIN_FEE;
import static im.mak.paddle.Node.node;

@SuppressWarnings("unused")
public class Asset extends AssetId implements Token {

    private final int decimals;
    private final PublicKey issuer;
    private final TransactionInfo originTransactionInfo;
    private final boolean isNft;
    
    public static Asset as(AssetId assetId) {
        return new Asset(assetId);
    }

    public Asset(AssetId assetId) {
        this(assetId.toString());
    }
    
    public Asset(String assetId) {
        super(assetId);

        AssetDetails details = this.getDetails();
        this.decimals = details.decimals();
        this.issuer = details.issuerPublicKey();

        this.originTransactionInfo = node().getTransactionInfo(details.originTransactionId());

        if (originTransactionInfo().tx() instanceof IssueTransaction) {
            IssueTransaction issue = (IssueTransaction) originTransactionInfo().tx();
            this.isNft = issue.quantity() == 1 && issue.decimals() == 0 && !issue.reissuable();
        } else if (originTransactionInfo().tx() instanceof InvokeScriptTransaction) {
            StateChanges stateChanges = node().getStateChanges(originTransactionInfo().tx().id()).stateChanges();
            IssueAction issue = stateChanges.issues().stream()
                    .filter(i -> this.equals(i.assetId()))
                    .findFirst()
                    .orElseThrow(IllegalStateException::new);
            this.isNft = issue.quantity() == 1 && issue.decimals() == 0 && !issue.isReissuable();
        } else
            throw new IllegalStateException("Can't find transaction which issued asset \"" + this + "\"");
    }

    @Override
    public AssetId id() {
        return this;
    }

    @Override
    public int decimals() {
        return this.decimals;
    }

    public PublicKey issuer() {
        return this.issuer;
    }

    public TransactionInfo originTransactionInfo() {
        return this.originTransactionInfo;
    }

    public boolean isNft() {
        return this.isNft;
    }

    public long amountSponsored(long wavesAmount) {
        long minSponsoredFee = this.getDetails().minSponsoredAssetFee();
        long increment = wavesAmount % MIN_FEE == 0 ? 0 : minSponsoredFee;
        return (wavesAmount / MIN_FEE) * minSponsoredFee + increment;
    }

    public Amount ofSponsored(long wavesAmount) {
        return Amount.of(this.amountSponsored(wavesAmount), this);
    }

    public AssetDetails getDetails() {
        return node().getAssetDetails(this);
    }

    @Override
    public long getQuantity() {
        return getDetails().quantity();
    }

    @Override
    public long getBalanceOf(Recipient recipient) {
        return node().getAssetBalance(RecipientResolver.toAddress(recipient), this);
    }

    public AssetDistribution getDistribution(int height, int limit, Address after) {
        return node().getAssetDistribution(this, height, limit, after);
    }

    public AssetDistribution getDistribution(int height, int limit) {
        return node().getAssetDistribution(this, height, limit);
    }

    public AssetDistribution getDistribution(int height) {
        return node().getAssetDistribution(this, height);
    }

    public AssetDistribution getDistribution() {
        return node().getAssetDistribution(this, node().getHeight());
    }

    public int getNextHeightToUpdateInfo() {
        int issueHeight = this.originTransactionInfo.height();
        int currentHeight = node().getHeight();
        int oneIntervalAgoHeight = currentHeight - node().minAssetInfoUpdateInterval();

        AtomicInteger lastUpdateHeight = new AtomicInteger();
        int listSize;
        Id afterTx = null;

        do {
            List<TransactionInfo> txs = node().getTransactionsByAddress(issuer.address(), 1000, afterTx);
            listSize = txs.size();

            if (txs.get(0).height() < oneIntervalAgoHeight)
                return currentHeight;

            txs.stream()
                    .filter(i -> i.tx() instanceof UpdateAssetInfoTransaction
                            && ((UpdateAssetInfoTransaction) i.tx()).assetId().equals(this))
                    .findFirst()
                    .ifPresent(i -> lastUpdateHeight.set(i.height()));

            if (lastUpdateHeight.get() > 0)
                break;
            else if (txs.get(listSize - 1).height() < issueHeight) {
                lastUpdateHeight.set(issueHeight);
                break;
            }

            afterTx = txs.get(listSize - 1).tx().id();
        } while (listSize == 1000);

        if (lastUpdateHeight.get() == 0)
            throw new IllegalStateException("Can't find height at last update info of asset \"" + this + "\"");
        else
            return lastUpdateHeight.get() + node().minAssetInfoUpdateInterval();
    }

}
