package im.mak.paddle.e2e;

import im.mak.paddle.Account;
import im.mak.paddle.api.exceptions.ApiError;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static im.mak.paddle.Node.node;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ApiErrorTest {

    private Account alice;
    private String assetId;

    @BeforeAll
    void before() {
        alice = new Account(10_00000000L);

        assetId = alice.issues(i -> i.name("Asset").quantity(1000_00000000L)).getId().toString();
    }

    @Test
    void a() {
        assertThat(node().api.assetDetails(assetId).name).isEqualTo("Asset");
    }

    @Test
    void b() {
        ApiError e = assertThrows(ApiError.class, () ->
                System.out.println("result -> " + node().api.assetDetails("r3r3r3").name)
        );
        assertAll("error fields",
                () -> assertThat(e.error).isEqualTo(199),
                () -> assertThat(e.message).isEqualTo("Failed to find issue transaction by ID")
        );
    }

    /*TODO how to test?
    @Test
    void c() {
        Node unexistedNode = new Node("http://localhost:9999/", 'U', "some seed");

        NodeError e = assertThrows(NodeError.class, () ->
                unexistedNode.api.assetDetails(assetId)
        );
        assertThat(e.getMessage()).contains("Failed to connect to");
    }*/

}
