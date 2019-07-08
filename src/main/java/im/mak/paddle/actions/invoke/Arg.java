package im.mak.paddle.actions.invoke;

import com.wavesplatform.wavesj.ByteString;
import com.wavesplatform.wavesj.transactions.InvokeScriptTransaction;

public class Arg {

    public static InvokeScriptTransaction.FunctionalArg<String> arg(String value) {
        return new InvokeScriptTransaction.StringArg(value);
    }

    public static InvokeScriptTransaction.FunctionalArg<Long> arg(long value) {
        return new InvokeScriptTransaction.LongArg(value);
    }

    public static InvokeScriptTransaction.FunctionalArg<Boolean> arg(boolean value) {
        return new InvokeScriptTransaction.BooleanArg(value);
    }

    public static InvokeScriptTransaction.FunctionalArg<ByteString> arg(byte[] value) {
        return new InvokeScriptTransaction.BinaryArg(new ByteString(value));
    }

}
