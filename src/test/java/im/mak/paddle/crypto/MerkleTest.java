package im.mak.paddle.crypto;

import im.mak.paddle.Account;
import im.mak.paddle.DockerNode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

import static im.mak.paddle.Async.async;
import static im.mak.paddle.actions.invoke.Arg.arg;
import static im.mak.paddle.util.ScriptUtil.fromFile;
import static org.assertj.core.api.Assertions.assertThat;

class MerkleTest {

    private DockerNode node;
    private Account alice, bob;

    private List<byte[]> leafs;
    private Merkle tree;
    private byte[] source;

    @BeforeEach
    void before() {
        node= new DockerNode();

        leafs = new ArrayList<>();
        IntStream.range(0, new Random().nextInt(100)).forEach(i -> leafs.add(
                BigInteger.valueOf(i).toByteArray()
        ));
        tree = new Merkle(leafs);
//TODO        source = (String.join("", Collections.nCopies(122, "ё")) + "b").getBytes();

        async(
                () -> alice = new Account(node, 1_00000000),
                () -> bob = new Account(node, 1_00000000)
        );
        alice.setsScript(s -> s.script(fromFile("merkle.ride")));
    }

    @AfterEach
    void after() {
        node.shutdown();
    }

    @Test
    void merkle() {
        assertThat(source).hasSize(32767);

        bob.invokes(i -> i.dApp(alice)
                .function("checkMerkle", arg(/*TODO*/"proof")));

        assertThat(alice.dataBool("result")).isTrue();
    }

}
