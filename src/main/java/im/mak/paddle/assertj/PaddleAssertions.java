package im.mak.paddle.assertj;

import com.wavesplatform.wavesj.StateChanges;
import im.mak.paddle.Account;
import im.mak.paddle.exceptions.ApiError;
import org.assertj.core.api.AbstractThrowableAssert;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.function.Executable;

public abstract class PaddleAssertions {

    public static AccountAssert assertThat(Account actual) {
        return AccountAssert.assertThat(actual);
    }

    public static StateChangesAssert assertThat(StateChanges actual) {
        return StateChangesAssert.assertThat(actual);
    }

    public static AbstractThrowableAssert<?, ? extends Throwable> assertThrows(Executable executable) {
        return Assertions.assertThat(org.junit.jupiter.api.Assertions.assertThrows(ApiError.class, executable));
    }

}
