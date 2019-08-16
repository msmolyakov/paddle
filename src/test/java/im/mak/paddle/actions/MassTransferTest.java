package im.mak.paddle.actions;

import com.wavesplatform.wavesj.transactions.MassTransferTransaction;
import im.mak.paddle.Account;
import im.mak.paddle.DockerNode;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static im.mak.paddle.Async.async;
import static im.mak.paddle.Constants.EXTRA_FEE;
import static im.mak.paddle.actions.mass.Recipient.to;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class MassTransferTest {

    private DockerNode node;
    private Account alice, bob, carol, dave, eve;
    private String tokenId;

    @BeforeAll
    void before() {
        node = new DockerNode();

        async(
                () -> {
                    alice = new Account(node, 1_00000000L + 300000 + 100 + 700000);
                    tokenId = alice.issues(a -> a.quantity(1000).decimals(0).script("true")).getId().toString();
                },
                () -> {
                    bob = new Account(node);
                    carol = new Account(node);
                },
                () -> {
                    dave = new Account(node, 100000);
                    dave.createsAlias(a -> a.alias("dave"));
                },
                () -> {
                    eve = new Account(node, 100000);
                    eve.createsAlias(a -> a.alias("eves.alias.with.maximum.length"));
                }
        );
    }

    @AfterAll
    void after() {
        node.shutdown();
    }

    @Test
    void massTransferToAddressAndAlias() {
        MassTransferTransaction wavesTx = alice.massTransfers(mt -> mt
                .recipients(
                        to(bob, 10),
                        to(carol.address(), 20),
                        to("alias:R:dave", 30),
                        to("alias:R:eves.alias.with.maximum.length", 40)
        ));

        assertAll(
                () -> assertThat(wavesTx.getFee()).isEqualTo(300000),

                () -> assertThat(alice.balance()).isEqualTo(700000),
                () -> assertThat(bob.balance()).isEqualTo(10),
                () -> assertThat(carol.balance()).isEqualTo(20),
                () -> assertThat(dave.balance()).isEqualTo(30),
                () -> assertThat(eve.balance()).isEqualTo(40)
        );

        MassTransferTransaction tokenTx = alice.massTransfers(mt -> mt
                .asset(tokenId)
                .recipients(
                        to(bob, 100),
                        to(carol.address(), 200),
                        to("alias:R:dave", 300),
                        to("alias:R:eves.alias.with.maximum.length", 400)
                ));

        assertAll(
                () -> assertThat(tokenTx.getFee()).isEqualTo(300000 + EXTRA_FEE),
                () -> assertThat(alice.balance()).isEqualTo(0),

                () -> assertThat(alice.balance(tokenId)).isEqualTo(0),
                () -> assertThat(bob.balance(tokenId)).isEqualTo(100),
                () -> assertThat(carol.balance(tokenId)).isEqualTo(200),
                () -> assertThat(dave.balance(tokenId)).isEqualTo(300),
                () -> assertThat(eve.balance(tokenId)).isEqualTo(400)
        );
    }

}
