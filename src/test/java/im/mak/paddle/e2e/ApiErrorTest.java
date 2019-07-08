package im.mak.paddle.e2e;

import im.mak.paddle.Account;
import im.mak.paddle.Node;
import im.mak.paddle.Version;
import im.mak.paddle.Account;
import im.mak.paddle.Node;
import im.mak.paddle.Version;
import im.mak.paddle.api.exceptions.ApiError;
import im.mak.paddle.exceptions.NodeError;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static im.mak.paddle.Node.connectToNode;
import static im.mak.paddle.Node.runDockerNode;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;

@TestInstance(PER_CLASS)
class ApiErrorTest {

    private Node node;
    private Account alice;
    private String assetId;

    @BeforeAll
    void before() {
        node = runDockerNode(Version.MAINNET);

        alice = new Account(node, 10_00000000L);

        assetId = alice.issues(i -> i.name("Asset").quantity(1000_00000000L)).getId().toString();
    }

    @AfterAll
    void after() {
        node.stopDockerNode();
    }

    @Test
    void a() {
        assertThat(node.api.assetDetails(assetId).name).isEqualTo("Asset");
    }

    @Test
    void b() {
        ApiError e = assertThrows(ApiError.class, () ->
                System.out.println("result -> " + node.api.assetDetails("r3r3r3").name)
        );
        assertAll("error fields",
                () -> assertThat(e.error).isEqualTo(199),
                () -> assertThat(e.message).isEqualTo("Failed to find issue transaction by ID")
        );
    }

    @Test
    void c() {
        Node unexistedNode = connectToNode("http://localhost:9999/", 'U');

        NodeError e = assertThrows(NodeError.class, () ->
                unexistedNode.api.assetDetails(assetId)
        );
        assertThat(e.getMessage()).contains("Failed to connect to");
    }

}
