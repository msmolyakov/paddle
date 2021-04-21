package im.mak.paddle.e2e;

import com.wavesplatform.transactions.common.Amount;
import im.mak.paddle.Account;
import im.mak.paddle.dapps.WalletDApp;
import org.junit.jupiter.api.*;

import static im.mak.paddle.token.Waves.WAVES;
import static im.mak.paddle.util.Async.async;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.MethodOrderer.OrderAnnotation;

class WalletTest {

    private static WalletDApp wallet;
    private static Account alice, bob;

    @BeforeAll
    static void before() {
        async(
                () -> wallet = new WalletDApp(WAVES.amount(10)),
                () -> alice = new Account(WAVES.amount(10)),
                () -> bob = new Account(WAVES.amount(10))
        );
    }

    @Nested
    @TestMethodOrder(OrderAnnotation.class)
    class Positive {

        @Test @Order(10)
        void canDepositWaves() {
            long walletInitBalance = wallet.getWavesBalance();
            Amount amount = WAVES.of(0.1);

            alice.invoke(wallet.deposit(), amount);

            assertAll("data and balances",
                    () -> assertThat(wallet.getData().size()).isEqualTo(1),
                    () -> assertThat(wallet.getIntegerData(alice.address().toString())).isEqualTo(amount.value()),

                    () -> assertThat(wallet.getWavesBalance()).isEqualTo(walletInitBalance + amount.value())
            );
        }

        @Test @Order(20)
        void canDepositWavesTwice() {
            long prevDeposit = wallet.getIntegerData(alice.address().toString());
            Amount amount = WAVES.of(0.5);

            alice.invoke(wallet.deposit(), amount);

            assertAll("data",
                    () -> assertThat(wallet.getData().size()).isEqualTo(1),
                    () -> assertThat(wallet.getIntegerData(alice.address().toString())).isEqualTo(prevDeposit + amount.value())
            );
        }

        @Test @Order(30)
        void accountsStoredSeparately() {
            long aliceDeposit = wallet.getIntegerData(alice.address().toString());
            Amount amount = WAVES.of(2);

            bob.invoke(wallet.deposit(), amount);

            assertAll("data",
                    () -> assertThat(wallet.getData().size()).isEqualTo(2),
                    () -> assertThat(wallet.getIntegerData(alice.address().toString())).isEqualTo(aliceDeposit),
                    () -> assertThat(wallet.getIntegerData(bob.address().toString())).isEqualTo(amount.value())
            );
        }

        @Test @Order(40)
        void canWithdrawPartially() {
            long walletInitBalance = wallet.getWavesBalance();
            long aliceInitBalance = alice.getWavesBalance();
            long aliceDeposit = wallet.getIntegerData(alice.address().toString());
            long walletDeposit = wallet.getIntegerData(bob.address().toString());
            long amount = WAVES.amount(0.03);

            long invokeFee =
                    alice.invoke(wallet.withdraw(amount))
                            .tx().fee().value();

            assertAll("data and balances",
                    () -> assertThat(wallet.getData().size()).isEqualTo(2),
                    () -> assertThat(wallet.getIntegerData(alice.address().toString())).isEqualTo(aliceDeposit - amount),
                    () -> assertThat(wallet.getIntegerData(bob.address().toString())).isEqualTo(walletDeposit),

                    () -> assertThat(wallet.getWavesBalance()).isEqualTo(walletInitBalance - amount),
                    () -> assertThat(alice.getWavesBalance()).isEqualTo(aliceInitBalance + amount - invokeFee)
            );
        }

        @Test @Order(50)
        void canWithdrawAll() {
            long walletInitBalance = wallet.getWavesBalance();
            long aliceInitBalance = alice.getWavesBalance();
            long amount = wallet.getIntegerData(alice.address().toString());

            long invokeFee =
                    alice.invoke(wallet.withdraw(amount))
                            .tx().fee().value();

            assertAll("data and balances",
                    () -> assertThat(wallet.getData().size()).isEqualTo(2),
                    () -> assertThat(wallet.getIntegerData(alice.address().toString())).isEqualTo(0),

                    () -> assertThat(wallet.getWavesBalance()).isEqualTo(walletInitBalance - amount),
                    () -> assertThat(alice.getWavesBalance()).isEqualTo(aliceInitBalance + amount - invokeFee)
            );
        }

    }

}
