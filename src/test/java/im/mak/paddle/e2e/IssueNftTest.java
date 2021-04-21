package im.mak.paddle.e2e;

import im.mak.paddle.Account;
import com.wavesplatform.transactions.common.AssetId;
import org.junit.jupiter.api.*;

import static im.mak.paddle.util.Constants.EXTRA_FEE;
import static im.mak.paddle.util.Constants.MIN_FEE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class IssueNftTest {

    private Account alice, bob;

    @BeforeEach
    void before() {
        alice = new Account(10_00000000L);
        bob = new Account();
    }

    @Test
    void canIssueNft() {
        long initBalance = alice.getWavesBalance();

        AssetId nftId = alice.issueNft(i -> i.name("My NFT").description("My first NFT").script("true"))
                .tx().assetId();

        alice.transfer(bob, nftId, 1);

        assertAll("balances",
                () -> assertThat(alice.getWavesBalance()).isEqualTo(initBalance - MIN_FEE * 2 - EXTRA_FEE),
                () -> assertThat(alice.getAssetBalance(nftId)).isEqualTo(0),

                () -> assertThat(bob.getAssetBalance(nftId)).isEqualTo(1),
                () -> assertThat(bob.getNft()).hasSize(1)
        );
    }

}
