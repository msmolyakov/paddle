package im.mak.paddle.e2e;

import im.mak.paddle.Account;
import im.mak.paddle.exceptions.ApiError;
import com.wavesplatform.transactions.common.AssetId;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static im.mak.paddle.Node.node;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ApiErrorTest {

    private static AssetId assetId;

    @BeforeAll
    static void before() {
        Account alice = new Account(10_00000000L);

        assetId = alice.issue(i -> i.name("Asset").quantity(1000_00000000L)).tx().assetId();
    }

    @Test
    void a() {
        assertThat(node().getAssetDetails(assetId).name()).isEqualTo("Asset");
    }

    @Test
    void b() {
        ApiError e = assertThrows(ApiError.class, () ->
                System.out.println("result -> " + node().getAssetDetails(AssetId.as("r3r3r3")).name())
        );
        assertAll("error fields",
                () -> assertThat(e.getErrorCode()).isEqualTo(4007),
                () -> assertThat(e.getMessage()).isEqualTo("Invalid asset id")
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
