package im.mak.paddle.params;

import im.mak.paddle.Account;
import com.wavesplatform.transactions.MassTransferTransaction;
import com.wavesplatform.transactions.WavesConfig;
import com.wavesplatform.transactions.common.Alias;
import com.wavesplatform.transactions.common.AssetId;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static im.mak.paddle.util.Async.async;
import static im.mak.paddle.util.Constants.EXTRA_FEE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class MassTransferTest {

    private static Account alice, bob, carol, dave, eve;
    private static AssetId tokenId;
    private static Alias aliasShort;
    private static Alias aliasLong;

    @BeforeAll
    static void before() {
        WavesConfig.chainId('R');
        aliasShort = Alias.as("alias:R:dave");
        aliasLong = Alias.as("alias:R:eves.alias.with.maximum.length");

        async(
                () -> {
                    alice = new Account(1_00000000L + 300000 + 100 + 700000);
                    tokenId = alice.issue(a -> a.quantity(1000).decimals(0).script("true")).tx().assetId();
                },
                () -> {
                    bob = new Account();
                    carol = new Account();
                },
                () -> {
                    dave = new Account(100000);
                    dave.createAlias(aliasShort);
                },
                () -> {
                    eve = new Account(100000);
                    eve.createAlias(aliasLong.name());
                }
        );
    }

    @Test
    void massTransferToAddressAndAlias() {
        MassTransferTransaction wavesTx = alice.massTransfer(mt -> mt
                        .to(bob, 10)
                        .to(carol.address(), 20)
                        .to(aliasShort, 30)
                        .to(aliasLong, 40)).tx();

        assertAll(
                () -> assertThat(wavesTx.fee().value()).isEqualTo(300000),

                () -> assertThat(alice.getWavesBalance()).isEqualTo(700000),
                () -> assertThat(bob.getWavesBalance()).isEqualTo(10),
                () -> assertThat(carol.getWavesBalance()).isEqualTo(20),
                () -> assertThat(dave.getWavesBalance()).isEqualTo(30),
                () -> assertThat(eve.getWavesBalance()).isEqualTo(40)
        );

        MassTransferTransaction tokenTx = alice.massTransfer(mt -> mt
                        .assetId(tokenId)
                        .to(bob, 100)
                        .to(carol.address(), 200)
                        .to(aliasShort, 300)
                        .to(aliasLong, 400)).tx();

        assertAll(
                () -> assertThat(tokenTx.fee().value()).isEqualTo(300000 + EXTRA_FEE),
                () -> assertThat(alice.getWavesBalance()).isEqualTo(0),

                () -> assertThat(alice.getAssetBalance(tokenId)).isEqualTo(0),
                () -> assertThat(bob.getAssetBalance(tokenId)).isEqualTo(100),
                () -> assertThat(carol.getAssetBalance(tokenId)).isEqualTo(200),
                () -> assertThat(dave.getAssetBalance(tokenId)).isEqualTo(300),
                () -> assertThat(eve.getAssetBalance(tokenId)).isEqualTo(400)
        );
    }

}
