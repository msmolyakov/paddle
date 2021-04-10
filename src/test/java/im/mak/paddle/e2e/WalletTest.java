package im.mak.paddle.e2e;

import com.wavesplatform.transactions.common.Amount;
import im.mak.paddle.Account;
import im.mak.paddle.dapps.Wallet;
import org.junit.jupiter.api.*;

import static im.mak.paddle.Async.async;
import static im.mak.paddle.util.ScriptUtil.fromFile;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.MethodOrderer.Alphanumeric;

class WalletTest {

    private static Account alice, bob, carol;
    private static Wallet wallet;

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
        wallet = new Wallet(alice.address());
    }

    @Nested
    @TestMethodOrder(Alphanumeric.class)
    class Positive {

        @Test
        void a_canDepositWaves() {
            long aliceInitBalance = alice.getWavesBalance();
            long amount = 100;

            bob.invoke(wallet.deposit(), Amount.of(amount));

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

            bob.invoke(wallet.deposit(), i -> i.wavesPayment(amount));

            assertAll("data",
                    () -> assertThat(alice.getData().size()).isEqualTo(1),
                    () -> assertThat(alice.getIntegerData(bob.address().toString())).isEqualTo(prevDeposit + amount)
            );
        }

        @Test
        void c_accountsStoredSeparately() {
            long bobDeposit = alice.getIntegerData(bob.address().toString());
            long amount = 20;

            carol.invoke(wallet.deposit(), Amount.of(amount));

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

            long invokeFee =
                    bob.invoke(wallet.withdraw(amount))
                            .tx().fee().value();

            assertAll("data and balances",
                    () -> assertThat(alice.getData().size()).isEqualTo(2),
                    () -> assertThat(alice.getIntegerData(bob.address().toString())).isEqualTo(bobDeposit - amount),
                    () -> assertThat(alice.getIntegerData(carol.address().toString())).isEqualTo(carolDeposit),

                    () -> assertThat(alice.getWavesBalance()).isEqualTo(aliceInitBalance - amount),
                    () -> assertThat(bob.getWavesBalance()).isEqualTo(bobInitBalance + amount - invokeFee)
            );
        }

        @Test
        void e_canWithdrawAll() {
            long aliceInitBalance = alice.getWavesBalance();
            long bobInitBalance = bob.getWavesBalance();
            long amount = alice.getIntegerData(bob.address().toString());

            long invokeFee =
                    bob.invoke(wallet.withdraw(amount))
                            .tx().fee().value();

            assertAll("data and balances",
                    () -> assertThat(alice.getData().size()).isEqualTo(2),
                    () -> assertThat(alice.getIntegerData(bob.address().toString())).isEqualTo(0),

                    () -> assertThat(alice.getWavesBalance()).isEqualTo(aliceInitBalance - amount),
                    () -> assertThat(bob.getWavesBalance()).isEqualTo(bobInitBalance + amount - invokeFee)
            );
        }

    }

}
