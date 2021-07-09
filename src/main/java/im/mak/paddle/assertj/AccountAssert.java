package im.mak.paddle.assertj;

import com.wavesplatform.transactions.common.Amount;
import com.wavesplatform.transactions.common.AssetId;
import com.wavesplatform.transactions.common.Base64String;
import com.wavesplatform.transactions.data.*;
import com.wavesplatform.wavesj.AssetDetails;
import im.mak.paddle.Account;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

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

    public AccountAssert hasDataExactly(Consumer<DataEntries> expected) {
        isNotNull();

        var entries = new DataEntries();
        expected.accept(entries);

        Assertions.assertThat(actual.getData())
                .containsExactlyInAnyOrder(entries.dataEntries.toArray(new DataEntry[0]));

        return this;
    }

    public AccountAssert hasDataExactly(DataEntry... expected) {
        return hasDataExactly(de -> {
            for (DataEntry entry : expected)
                de.entry(entry);
        });
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

    public static class DataEntries {

        protected List<DataEntry> dataEntries;

        protected DataEntries() {
            dataEntries = new ArrayList<>();
        }

        public DataEntries entry(DataEntry dataEntry) {
            dataEntries.add(dataEntry);
            return this;
        }

        public DataEntries binary(String key, Base64String value) {
            dataEntries.add(BinaryEntry.as(key, value));
            return this;
        }

        public DataEntries binary(String key, byte[] value) {
            dataEntries.add(BinaryEntry.as(key, value));
            return this;
        }

        public DataEntries binary(String key, String value) {
            dataEntries.add(BinaryEntry.as(key, value));
            return this;
        }

        public DataEntries bool(String key, boolean value) {
            dataEntries.add(BooleanEntry.as(key, value));
            return this;
        }

        public DataEntries integer(String key, long value) {
            dataEntries.add(IntegerEntry.as(key, value));
            return this;
        }

        public DataEntries string(String key, String value) {
            dataEntries.add(StringEntry.as(key, value));
            return this;
        }

        public DataEntries delete(String key) {
            dataEntries.add(DeleteEntry.as(key));
            return this;
        }

    }

}
