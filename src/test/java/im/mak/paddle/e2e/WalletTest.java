package im.mak.paddle.e2e;

import com.wavesplatform.wavesj.Transaction;
import im.mak.paddle.Account;
import im.mak.paddle.Node;
import im.mak.paddle.Version;
import org.junit.jupiter.api.*;

import static im.mak.paddle.Node.runDockerNode;
import static im.mak.paddle.actions.invoke.Arg.arg;
import static im.mak.paddle.util.PathUtil.path;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.MethodOrderer.Alphanumeric;

@TestMethodOrder(Alphanumeric.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class WalletTest {

    private Node node;
    private Account alice, bob, carol;

    @BeforeAll
    void before() {
        node = runDockerNode(Version.MAINNET);

        alice = new Account(node, 1_00000000L);
        bob = new Account(node, 1_00000000L);
        carol = new Account(node, 1_00000000L);

        alice.setsScript(s -> s.script(path("wallet.ride")));
    }

    @AfterAll
    void after() {
        node.stopDockerNode();
    }

    @Nested
    @TestMethodOrder(Alphanumeric.class)
    class Positive {

        @Test
        void a_canDepositWaves() {
            long aliceInitBalance = alice.balance();
            long amount = 100;

            bob.invokes(i -> i.dApp(alice).function("deposit").wavesPayment(amount));

            assertAll("data and balances",
                    () -> assertThat(alice.data().size()).isEqualTo(1),
                    () -> assertThat(alice.dataInt(bob.address())).isEqualTo(amount),

                    () -> assertThat(alice.balance()).isEqualTo(aliceInitBalance + amount)
            );
        }

        @Test
        void b_canDepositWavesTwice() {
            long prevDeposit = alice.dataInt(bob.address());
            long amount = 50;

            bob.invokes(i -> i.dApp(alice).function("deposit").wavesPayment(amount));

            assertAll("data",
                    () -> assertThat(alice.data().size()).isEqualTo(1),
                    () -> assertThat(alice.dataInt(bob.address())).isEqualTo(prevDeposit + amount)
            );
        }

        @Test
        void c_accountsStoredSeparately() {
            long bobDeposit = alice.dataInt(bob.address());
            long amount = 20;

            carol.invokes(i -> i.dApp(alice).function("deposit").wavesPayment(amount));

            assertAll("data",
                    () -> assertThat(alice.data().size()).isEqualTo(2),
                    () -> assertThat(alice.dataInt(bob.address())).isEqualTo(bobDeposit),
                    () -> assertThat(alice.dataInt(carol.address())).isEqualTo(amount)
            );
        }

        @Test
        void d_canWithdrawPartially() {
            long aliceInitBalance = alice.balance();
            long bobInitBalance = bob.balance();
            long bobDeposit = alice.dataInt(bob.address());
            long carolDeposit = alice.dataInt(carol.address());
            long amount = 1;

            Transaction invoke = bob.invokes(i -> i.dApp(alice).function("withdraw", arg(amount)));

            assertAll("data and balances",
                    () -> assertThat(alice.data().size()).isEqualTo(2),
                    () -> assertThat(alice.dataInt(bob.address())).isEqualTo(bobDeposit - amount),
                    () -> assertThat(alice.dataInt(carol.address())).isEqualTo(carolDeposit),

                    () -> assertThat(alice.balance()).isEqualTo(aliceInitBalance - amount),
                    () -> assertThat(bob.balance()).isEqualTo(bobInitBalance + amount - invoke.getFee())
            );
        }

        @Test
        void e_canWithdrawAll() {
            long aliceInitBalance = alice.balance();
            long bobInitBalance = bob.balance();
            long amount = alice.dataInt(bob.address());

            Transaction invoke = bob.invokes(i -> i.dApp(alice).function("withdraw", arg(amount)));

            assertAll("data and balances",
                    () -> assertThat(alice.data().size()).isEqualTo(2),
                    () -> assertThat(alice.dataInt(bob.address())).isEqualTo(0),

                    () -> assertThat(alice.balance()).isEqualTo(aliceInitBalance - amount),
                    () -> assertThat(bob.balance()).isEqualTo(bobInitBalance + amount - invoke.getFee())
            );
        }

    }

}
