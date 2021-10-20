package im.mak.paddle;

import org.junit.jupiter.api.*;

import static im.mak.paddle.Node.node;
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

}
