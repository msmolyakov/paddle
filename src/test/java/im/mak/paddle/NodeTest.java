package im.mak.paddle;

import com.wavesplatform.wavesj.ScriptInfo;
import org.junit.jupiter.api.*;

import static im.mak.paddle.Node.node;
import static im.mak.paddle.util.ScriptUtil.fromFile;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NodeTest {

    @Nested
    class WaitForHeight {

        @Test
        void canWaitForHeight() {
            int current = node().getHeight();

            int height = node().waitForHeight(current + 1);

            assertThat(height).isGreaterThanOrEqualTo(current + 1);
        }

        @Test
        void errorIfCantReachTheHeight() {
            int current = node().getHeight();

            assertThat(
                    assertThrows(RuntimeException.class, () ->
                            node().waitForHeight(current + 100, 1))
            ).hasMessageEndingWith(
                    "Could not wait for the height to rise from " + current + " to " + (current + 100) +
                            ": height " + current + " did not grow for 1 seconds");
        }

    }

    @Nested
    class WaitNBlocks {

        @Test
        void canWaitNBlocks() {
            int current = node().getHeight();

            int height = node().waitBlocks(1);

            assertThat(height).isGreaterThanOrEqualTo(current + 1);
        }

        @Test
        void errorIfCantWaitNBlocks() {
            int current = node().getHeight();

            assertThat(
                    assertThrows(RuntimeException.class, () ->
                            node().waitBlocks(100, 1))
            ).hasMessageEndingWith(
                    "Could not wait for the height to rise from " + current + " to " + (current + 100) +
                            ": height " + current + " did not grow for 1 seconds");
        }

    }

    @Nested
    class CompileScript {
        private final String INITIAL_SCRIPT = fromFile("wallet.ride");
        private final String ERROR_SCRIPT = fromFile("wallet.ride").replace("i.payment", "v.payment");

        @Test
        void canCompileScript() {
            ScriptInfo scriptInfo = node().compileScript(INITIAL_SCRIPT);
            assertThat(scriptInfo.script()).isNotEqualTo("");
        }

        @Test
        void canCompileScriptCompacted() {
            ScriptInfo scriptInfo = node().compileScript(INITIAL_SCRIPT, true);
            assertThat(scriptInfo.script()).isNotEqualTo("");
        }

        @Test
        void cantCompileScriptWithError() {
            assertThat(
                    assertThrows(RuntimeException.class, () ->
                            node().compileScript(ERROR_SCRIPT, true))
            ).hasMessageContaining(
                    " A definition of 'v' is not found");
        }

    }

}
