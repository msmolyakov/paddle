package im.mak.paddle.token;

import com.wavesplatform.transactions.account.PrivateKey;
import com.wavesplatform.transactions.common.Amount;
import com.wavesplatform.transactions.common.AssetId;
import com.wavesplatform.transactions.common.Recipient;
import im.mak.paddle.Account;

import java.math.BigDecimal;

public interface Token {

    AssetId id();
    int decimals();
    long getQuantity();
    long getBalanceOf(Recipient recipient);

    default long toCoins(double tokens) {
        return BigDecimal.valueOf(tokens).multiply(BigDecimal.valueOf(Math.pow(10, decimals()))).longValueExact();
    }

    @Deprecated
    default long amount(double amount) {
        return toCoins(amount);
    }

    default Amount ofCoins(long coins) {
        return Amount.of(coins, id());
    }

    default Amount ofTokens(double tokens) {
        return ofCoins(amount(tokens));
    }

    @Deprecated
    default Amount of(double amount) {
        return ofTokens(amount);
    }

    default long getBalanceOf(PrivateKey account) {
        return getBalanceOf(account.address());
    }

    default long getBalanceOf(Account account) {
        return this.getBalanceOf(account.privateKey());
    }

}
