package im.mak.paddle.assertj;

import com.wavesplatform.transactions.common.Amount;
import com.wavesplatform.transactions.common.AssetId;
import com.wavesplatform.transactions.data.DataEntry;
import com.wavesplatform.wavesj.AssetDetails;
import im.mak.paddle.Account;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;

import java.util.List;

import static java.util.stream.Collectors.toList;

public class AccountAssert extends AbstractAssert<AccountAssert, Account> {

    public AccountAssert(Account actual) {
        super(actual, AccountAssert.class);
    }

    public static AccountAssert assertThat(Account actual) {
        return new AccountAssert(actual);
    }

    public AccountAssert hasNoData() {
        isNotNull();

        Assertions.assertThat(actual.getData()).isEmpty();

        return this;
    }

    public AccountAssert hasDataExactly(DataEntry... expected) {
        isNotNull();

        Assertions.assertThat(actual.getData()).containsExactlyInAnyOrder(expected);

        return this;
    }

    public AccountAssert hasBalance(Amount expected) {
        isNotNull();

        long actualBalance = expected.assetId().isWaves() ? actual.getWavesBalance() : actual.getAssetBalance(expected.assetId());

        Assertions.assertThat(actualBalance).isEqualTo(expected.value());

        return this;
    }

    public AccountAssert hasAssetsExactly(Amount... expected) {
        isNotNull();

        List<Amount> balances = actual.getAssetsBalance()
                .stream()
                .map(b -> Amount.of(b.balance(), b.assetId()))
                .collect(toList());

        Assertions.assertThat(balances).containsExactlyInAnyOrder(expected);

        return this;
    }

    public AccountAssert hasNftExactly(AssetId... expected) {
        isNotNull();

        //todo iterator over 1000 nft
        List<AssetId> nftList = actual.getNft()
                .stream()
                .map(AssetDetails::assetId)
                .collect(toList());

        Assertions.assertThat(nftList).containsExactlyInAnyOrder(expected);

        return this;
    }

}
