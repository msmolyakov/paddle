package im.mak.paddle.token;

import com.wavesplatform.transactions.account.PrivateKey;
import com.wavesplatform.transactions.common.AssetId;
import com.wavesplatform.transactions.common.Recipient;
import com.wavesplatform.wavesj.BalanceDetails;
import im.mak.paddle.Account;
import im.mak.paddle.util.RecipientResolver;

import static im.mak.paddle.Node.node;

public class Waves implements Token {

    private static final AssetId ID = AssetId.WAVES;
    private static final int DECIMALS = 8;

    @Override
    public AssetId id() {
        return ID;
    }

    @Override
    public int decimals() {
        return DECIMALS;
    }

    @Override
    public long getQuantity() {
        return node().getBlockchainRewards().totalWavesAmount();
    }

    @Override
    public long getBalanceOf(Recipient recipient) {
        return node().getBalance(RecipientResolver.toAddress(recipient));
    }

    public BalanceDetails getBalanceDetailsOf(Recipient recipient) {
        return node().getBalanceDetails(RecipientResolver.toAddress(recipient));
    }

    public BalanceDetails getBalanceDetailsOf(PrivateKey account) {
        return getBalanceDetailsOf(account.address());
    }

    public BalanceDetails getBalanceDetailsOf(Account account) {
        return getBalanceDetailsOf(account.privateKey());
    }

    //TODO getDistribution(height)

}
