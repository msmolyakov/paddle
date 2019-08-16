package im.mak.paddle.e2e;

import im.mak.paddle.Account;
import im.mak.paddle.DockerNode;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static im.mak.paddle.Constants.MIN_FEE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class IssueNftTest {

    private DockerNode node;
    private Account alice, bob;

    @BeforeAll
    void before() {
        node = new DockerNode();

        alice = new Account(node, 10_00000000L);
        bob = new Account(node);
    }

    @AfterAll
    void after() {
        node.shutdown();
    }

    @Test
    void canIssueNft() {
        long initBalance = alice.balance();

        String nftId = alice.issuesNft(i -> i.name("My NFT").description("My first NFT").script("true"))
                .getId().toString();

        alice.transfers(t -> t.to(bob).amount(1).asset(nftId));

        assertAll("balances",
                () -> assertThat(alice.balance()).isEqualTo(initBalance - MIN_FEE * 6),
                () -> assertThat(alice.balance(nftId)).isEqualTo(0),

                () -> assertThat(bob.balance(nftId)).isEqualTo(1),
                () -> assertThat(bob.nft()).hasSize(1)
        );
    }

}
