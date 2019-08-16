package im.mak.paddle;

import im.mak.paddle.exceptions.NodeError;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NodeTest {

    private DockerNode node;

    @BeforeAll
    void before() {
        node = new DockerNode();
    }

    @AfterAll
    void after() {
        node.shutdown();
    }

    @Nested
    class WaitForHeight {

        @Test
        void canWaitForHeight() {
            int current = node.height();

            int height = node.waitForHeight(current + 1);

            assertThat(height).isGreaterThanOrEqualTo(current + 1);
        }

        @Test
        void errorIfCantReachTheHeight() {
            int current = node.height();

            NodeError e = assertThrows(NodeError.class, () ->
                    node.waitForHeight(current + 100, 1)
            );

            assertThat(e).hasMessageStartingWith(
                    "Could not wait for height " + (current + 100) + " in 1 seconds. Current height:");
        }

    }

    @Nested
    class WaitNBlocks {

        @Test
        void canWaitNBlocks() {
            int current = node.height();

            int height = node.waitNBlocks(1);

            assertThat(height).isGreaterThanOrEqualTo(current + 1);
        }

        @Test
        void errorIfCantWaitNBlocks() {
            int current = node.height();

            NodeError e = assertThrows(NodeError.class, () ->
                    node.waitNBlocks(100, 1)
            );

            assertThat(e).hasMessageStartingWith(
                    "Could not wait for height " + (current + 100) + " in 1 seconds. Current height:");
        }

    }

}
