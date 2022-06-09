package im.mak.paddle.assertj;

import com.wavesplatform.transactions.common.Amount;
import com.wavesplatform.wavesj.AssetBalance;
import com.wavesplatform.wavesj.StateChanges;
import im.mak.paddle.Account;
import im.mak.paddle.exceptions.ApiError;
import org.assertj.core.api.AbstractThrowableAssert;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;

public abstract class PaddleAssertions extends Assertions {

    public static AccountAssert assertThat(Account actual) {
        return AccountAssert.assertThat(actual);
    }

    public static StateChangesAssert assertThat(StateChanges actual) {
        return StateChangesAssert.assertThat(actual);
    }

    public static AssetBalanceAssert assertThat(AssetBalance actual) {
        return AssetBalanceAssert.assertThat(actual);
    }

    public static AbstractThrowableAssert<?, ? extends Throwable> assertThrows(ThrowingCallable executable) {
        return assertThat(catchThrowableOfType(executable, ApiError.class));
    }

}
