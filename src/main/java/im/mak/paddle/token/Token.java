package im.mak.paddle.token;

import com.wavesplatform.transactions.account.PrivateKey;
import com.wavesplatform.transactions.common.Amount;
import com.wavesplatform.transactions.common.AssetId;
import com.wavesplatform.transactions.common.Recipient;
import im.mak.paddle.Account;

public interface Token {

    AssetId id();
    int decimals();
    long getQuantity();
    long getBalanceOf(Recipient recipient);

    default long amount(double amount) {
        return (long) (amount * (Math.pow(10L, decimals())));
    }

    default Amount of(double amount) {
        return Amount.of(amount(amount), id());
    }

    default long getBalanceOf(PrivateKey account) {
        return getBalanceOf(account.address());
    }

    default long getBalanceOf(Account account) {
        return this.getBalanceOf(account.privateKey());
    }

}
