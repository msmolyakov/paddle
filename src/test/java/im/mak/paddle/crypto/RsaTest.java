package im.mak.paddle.crypto;

import im.mak.paddle.Account;
import im.mak.paddle.DockerNode;
import im.mak.paddle.crypto.Rsa;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static im.mak.paddle.Async.async;
import static im.mak.paddle.util.ScriptUtil.fromFile;
import static im.mak.paddle.crypto.HashAlg.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class RsaTest {

    private DockerNode node;
    private Account alice, bob;

    private Rsa rsa;
    private byte[] source245b, source32kb;

    @BeforeEach
    void before() {
        node= new DockerNode();

        rsa = new Rsa();
        source245b = (String.join("", Collections.nCopies(122, "ё")) + "b").getBytes();
        source32kb = (String.join("", Collections.nCopies(16382, "ё")) + "bbb").getBytes();

        async(
                () -> alice = new Account(node, 1_00000000),
                () -> bob = new Account(node, 1_00000000)
        );
        alice.setsScript(s -> s.script(fromFile("rsa.ride")));
    }

    @AfterEach
    void after() {
        node.shutdown();
    }

    @Test
    void rsa() {
        assertAll(
                () -> assertThat(source245b).hasSize(245),
                () -> assertThat(source32kb).hasSize(32767)
        );

        alice.writes(d -> d.fee(20000000)
                .binary("source-245-bytes", source245b)
                .binary("source-32-kb", source32kb)
                .binary("public-key", rsa.keys.getPublic().getEncoded())

                .binary("proof_none", rsa.sign(NONE, source245b))
                .binary("proof_md5", rsa.sign(MD5, source32kb))
                .binary("proof_sha1", rsa.sign(SHA1, source32kb))

                .binary("proof_sha224", rsa.sign(SHA224, source32kb))
                .binary("proof_sha256", rsa.sign(SHA256, source32kb))
                .binary("proof_sha384", rsa.sign(SHA384, source32kb))
                .binary("proof_sha512", rsa.sign(SHA512, source32kb))

                .binary("proof_sha3-224", rsa.sign(SHA3_224, source32kb))
                .binary("proof_sha3-256", rsa.sign(SHA3_256, source32kb))
                .binary("proof_sha3-384", rsa.sign(SHA3_384, source32kb))
                .binary("proof_sha3-512", rsa.sign(SHA3_512, source32kb))
        );

        async(
                () -> bob.invokes(i -> i.dApp(alice).function("group1")),
                () -> bob.invokes(i -> i.dApp(alice).function("group2")),
                () -> bob.invokes(i -> i.dApp(alice).function("group3"))
        );

        assertAll(
                () -> assertThat(alice.dataBool("result_none")).isTrue(),
                () -> assertThat(alice.dataBool("result_md5")).isTrue(),
                () -> assertThat(alice.dataBool("result_sha1")).isTrue(),

                () -> assertThat(alice.dataBool("result_sha224")).isTrue(),
                () -> assertThat(alice.dataBool("result_sha256")).isTrue(),
                () -> assertThat(alice.dataBool("result_sha384")).isTrue(),
                () -> assertThat(alice.dataBool("result_sha512")).isTrue(),

                () -> assertThat(alice.dataBool("result_sha3-224")).isTrue(),
                () -> assertThat(alice.dataBool("result_sha3-256")).isTrue(),
                () -> assertThat(alice.dataBool("result_sha3-384")).isTrue(),
                () -> assertThat(alice.dataBool("result_sha3-512")).isTrue()
        );
    }

}
