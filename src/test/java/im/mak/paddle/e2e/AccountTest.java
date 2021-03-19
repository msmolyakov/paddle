package im.mak.paddle.e2e;

import im.mak.paddle.Account;
import com.wavesplatform.transactions.common.Base64String;
import com.wavesplatform.transactions.data.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static im.mak.paddle.Async.async;
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
        Base64String binary = new Base64String("hello".getBytes());

        async(
                () -> alice.writeData(d -> d
                        .binary("bin", binary)
                        .bool("bool1", true)
                        .bool("bool2", false)
                        .integer("int", 100500)
                        .string("str", "привёт")
                ),
                () -> bob.writeData(d -> d
                        .data(
                                BinaryEntry.as("bin", binary),
                                BooleanEntry.as("bool1", true),
                                BooleanEntry.as("bool2", false),
                                IntegerEntry.as("int", 100500),
                                StringEntry.as("str", "привёт")
                        ))
        );

        BinaryEntry dataBin = (BinaryEntry) alice.getData().stream().filter(d -> d.key().equals("bin")).findFirst().get();
        BooleanEntry dataBool1 = (BooleanEntry) alice.getData().stream().filter(d -> d.key().equals("bool1")).findFirst().get();
        BooleanEntry dataBool2 = (BooleanEntry) alice.getData().stream().filter(d -> d.key().equals("bool2")).findFirst().get();
        IntegerEntry dataInt = (IntegerEntry) alice.getData().stream().filter(d -> d.key().equals("int")).findFirst().get();
        StringEntry dataStr = (StringEntry) alice.getData().stream().filter(d -> d.key().equals("str")).findFirst().get();

        assertAll(
                () -> assertThat(dataBin.value()).isEqualTo(binary),
                () -> assertThat(dataBool1.value()).isTrue(),
                () -> assertThat(dataBool2.value()).isFalse(),
                () -> assertThat(dataInt.value()).isEqualTo(100500),
                () -> assertThat(dataStr.value()).isEqualTo("привёт"),

                () -> assertThat(alice.getBinaryData("bin")).isEqualTo(binary),
                () -> assertThat(alice.getBooleanData("bool1")).isTrue(),
                () -> assertThat(alice.getBooleanData("bool2")).isFalse(),
                () -> assertThat(alice.getIntegerData("int")).isEqualTo(100500),
                () -> assertThat(alice.getStringData("str")).isEqualTo("привёт"),

                () -> assertThat(bob.getBinaryData("bin")).isEqualTo(binary),
                () -> assertThat(bob.getBooleanData("bool1")).isTrue(),
                () -> assertThat(bob.getBooleanData("bool2")).isFalse(),
                () -> assertThat(bob.getIntegerData("int")).isEqualTo(100500),
                () -> assertThat(bob.getStringData("str")).isEqualTo("привёт")
        );
    }

}
