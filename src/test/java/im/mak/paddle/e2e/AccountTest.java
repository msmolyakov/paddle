package im.mak.paddle.e2e;

import com.wavesplatform.wavesj.Base58;
import com.wavesplatform.wavesj.DataEntry;
import im.mak.paddle.Account;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static im.mak.paddle.Async.async;
import static im.mak.paddle.actions.data.Entry.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class AccountTest {

    private Account alice, bob;

    @BeforeEach
    void before() {
        async(
                () -> alice = new Account(10_00000000L),
                () -> bob = new Account(10_00000000L)
        );
    }

    @Test
    void dataStorage() {
        byte[] binary = "hello".getBytes();

        async(
                () -> alice.writes(d -> d
                        .binary("bin", binary)
                        .bool("bool1", true)
                        .bool("bool2", false)
                        .integer("int", 100500)
                        .string("str", "привёт")
                ),
                () -> bob.writes(d -> d
                        .data(
                                binary("bin", binary),
                                bool("bool1", true),
                                bool("bool2", false),
                                integer("int", 100500),
                                string("str", "привёт")
                        ))
        );

        DataEntry dataBin = alice.data().stream().filter(d -> d.getKey().equals("bin")).findFirst().get();
        DataEntry dataBool1 = alice.data().stream().filter(d -> d.getKey().equals("bool1")).findFirst().get();
        DataEntry dataBool2 = alice.data().stream().filter(d -> d.getKey().equals("bool2")).findFirst().get();
        DataEntry dataInt = alice.data().stream().filter(d -> d.getKey().equals("int")).findFirst().get();
        DataEntry dataStr = alice.data().stream().filter(d -> d.getKey().equals("str")).findFirst().get();

        assertAll(
                () -> assertThat(Base58.decode(dataBin.getValue().toString())).isEqualTo(binary),
                () -> assertThat((boolean) dataBool1.getValue()).isTrue(),
                () -> assertThat((boolean) dataBool2.getValue()).isFalse(),
                () -> assertThat((long) dataInt.getValue()).isEqualTo(100500),
                () -> assertThat(dataStr.getValue().toString()).isEqualTo("привёт"),

                () -> assertThat(alice.dataBin("bin")).isEqualTo(binary),
                () -> assertThat(alice.dataBool("bool1")).isTrue(),
                () -> assertThat(alice.dataBool("bool2")).isFalse(),
                () -> assertThat(alice.dataInt("int")).isEqualTo(100500),
                () -> assertThat(alice.dataStr("str")).isEqualTo("привёт"),

                () -> assertThat(bob.dataBin("bin")).isEqualTo(binary),
                () -> assertThat(bob.dataBool("bool1")).isTrue(),
                () -> assertThat(bob.dataBool("bool2")).isFalse(),
                () -> assertThat(bob.dataInt("int")).isEqualTo(100500),
                () -> assertThat(bob.dataStr("str")).isEqualTo("привёт")
        );
    }

}
