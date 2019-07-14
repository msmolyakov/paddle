package im.mak.paddle.e2e;

import im.mak.paddle.Account;
import im.mak.paddle.Node;
import im.mak.paddle.Version;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static im.mak.paddle.Constants.MIN_FEE;
import static im.mak.paddle.Node.runDockerNode;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class IssueNftTest {

    private Node node;
    private Account alice;

    @BeforeAll
    void before() {
        node = runDockerNode(Version.MAINNET);

        alice = new Account(node, 1_00000000L);
    }

    @AfterAll
    void after() {
        node.stopDockerNode();
    }

    @Test
    void canIssueNft() {
        long initBalance = alice.balance();

        String nftId = alice.issuesNft(i -> i.name("NFT").description("My first NFT").script("true"))
                .getId().toString();

        assertAll("balances",
                () -> assertThat(alice.balance()).isEqualTo(initBalance - MIN_FEE),
                () -> assertThat(alice.balance(nftId)).isEqualTo(1)
                //TODO check API nft by address
        );
    }

}
