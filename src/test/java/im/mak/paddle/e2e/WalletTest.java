package im.mak.paddle.e2e;

import im.mak.paddle.Account;
import im.mak.waves.transactions.InvokeScriptTransaction;
import im.mak.waves.transactions.invocation.IntegerArg;
import org.junit.jupiter.api.*;

import static im.mak.paddle.Async.async;
import static im.mak.paddle.util.ScriptUtil.fromFile;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.MethodOrderer.Alphanumeric;

class WalletTest {

    private static Account alice, bob, carol;

    @BeforeAll
    static void before() {
        async(
                () -> {
                    alice = new Account(1_00000000L);
                    alice.setScript(s -> s.script(fromFile("wallet.ride")));
                },
                () -> bob = new Account(1_00000000L),
                () -> carol = new Account(1_00000000L)
        );
    }

    @Nested
    @TestMethodOrder(Alphanumeric.class)
    class Positive {

        @Test
        void a_canDepositWaves() {
            long aliceInitBalance = alice.getWavesBalance();
            long amount = 100;

            bob.invoke(i -> i.dApp(alice).function("deposit").wavesPayment(amount));

            assertAll("data and balances",
                    () -> assertThat(alice.getData().size()).isEqualTo(1),
                    () -> assertThat(alice.getIntegerData(bob.address().toString())).isEqualTo(amount),

                    () -> assertThat(alice.getWavesBalance()).isEqualTo(aliceInitBalance + amount)
            );
        }

        @Test
        void b_canDepositWavesTwice() {
            long prevDeposit = alice.getIntegerData(bob.address().toString());
            long amount = 50;

            bob.invoke(i -> i.dApp(alice).function("deposit").wavesPayment(amount));

            assertAll("data",
                    () -> assertThat(alice.getData().size()).isEqualTo(1),
                    () -> assertThat(alice.getIntegerData(bob.address().toString())).isEqualTo(prevDeposit + amount)
            );
        }

        @Test
        void c_accountsStoredSeparately() {
            long bobDeposit = alice.getIntegerData(bob.address().toString());
            long amount = 20;

            carol.invoke(i -> i.dApp(alice).function("deposit").wavesPayment(amount));

            assertAll("data",
                    () -> assertThat(alice.getData().size()).isEqualTo(2),
                    () -> assertThat(alice.getIntegerData(bob.address().toString())).isEqualTo(bobDeposit),
                    () -> assertThat(alice.getIntegerData(carol.address().toString())).isEqualTo(amount)
            );
        }

        @Test
        void d_canWithdrawPartially() {
            long aliceInitBalance = alice.getWavesBalance();
            long bobInitBalance = bob.getWavesBalance();
            long bobDeposit = alice.getIntegerData(bob.address().toString());
            long carolDeposit = alice.getIntegerData(carol.address().toString());
            long amount = 1;

            InvokeScriptTransaction invoke = bob.invoke((i -> i.dApp(alice).function("withdraw", IntegerArg.as(amount)))).tx();

            assertAll("data and balances",
                    () -> assertThat(alice.getData().size()).isEqualTo(2),
                    () -> assertThat(alice.getIntegerData(bob.address().toString())).isEqualTo(bobDeposit - amount),
                    () -> assertThat(alice.getIntegerData(carol.address().toString())).isEqualTo(carolDeposit),

                    () -> assertThat(alice.getWavesBalance()).isEqualTo(aliceInitBalance - amount),
                    () -> assertThat(bob.getWavesBalance()).isEqualTo(bobInitBalance + amount - invoke.fee().value())
            );
        }

        @Test
        void e_canWithdrawAll() {
            long aliceInitBalance = alice.getWavesBalance();
            long bobInitBalance = bob.getWavesBalance();
            long amount = alice.getIntegerData(bob.address().toString());

            InvokeScriptTransaction invoke = bob.invoke((i -> i.dApp(alice).function("withdraw", IntegerArg.as(amount)))).tx();

            assertAll("data and balances",
                    () -> assertThat(alice.getData().size()).isEqualTo(2),
                    () -> assertThat(alice.getIntegerData(bob.address().toString())).isEqualTo(0),

                    () -> assertThat(alice.getWavesBalance()).isEqualTo(aliceInitBalance - amount),
                    () -> assertThat(bob.getWavesBalance()).isEqualTo(bobInitBalance + amount - invoke.fee().value())
            );
        }

    }

}
