package im.mak.paddle.ride;

import com.wavesplatform.lang.Lang;
import com.wavesplatform.lang.v1.compiler.Terms;

public class Ride {

    public Ride() {
        Terms.EXPR result = Lang.compile("true");
        System.out.println();

        /*Repl repl = Repl.apply((StdLibVersion) V3.value());
        System.out.println(repl.ver());*/
    }

}
