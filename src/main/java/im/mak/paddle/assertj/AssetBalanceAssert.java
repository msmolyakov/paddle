package im.mak.paddle.assertj;

import com.wavesplatform.transactions.common.Amount;
import com.wavesplatform.wavesj.AssetBalance;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.SoftAssertions;

public class AssetBalanceAssert extends AbstractAssert<AssetBalanceAssert, AssetBalance> {

    public static AssetBalanceAssert assertThat(AssetBalance actual) {
        return new AssetBalanceAssert(actual);
    }

    protected AssetBalanceAssert(AssetBalance amount) {
        super(amount, AssetBalanceAssert.class);
    }

    public AssetBalanceAssert isEqualToAmount(Amount expected) {
        isNotNull();

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(actual.assetId()).as("Asset ID").isEqualTo(expected.assetId());
        softly.assertThat(actual.balance()).as("Balance").isEqualTo(expected.value());
        softly.assertAll();

        return this;
    }

}
