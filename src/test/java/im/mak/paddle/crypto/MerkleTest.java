package im.mak.paddle.crypto;

import im.mak.paddle.Account;
import im.mak.paddle.DockerNode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.stream.Stream;

import static im.mak.paddle.Async.async;
import static im.mak.paddle.actions.invoke.Arg.arg;
import static im.mak.paddle.util.ScriptUtil.fromFile;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class MerkleTest {

    private DockerNode node;
    private Account alice, bob;

    @BeforeEach
    void before() {
        node = new DockerNode();

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
        Merkle tree0 = new Merkle(Stream.of("one")
                .map(String::getBytes).collect(toList()));
        String txId0 = bob.invokes(i -> i.dApp(alice).function("checkMerkle",
                arg(tree0.rootHash()), arg(tree0.proofByLeafIndex(0).get()), arg("one".getBytes())))
                .getId().toString();
        assertThat(alice.dataBool(txId0)).isTrue();


        Merkle tree1 = new Merkle(Stream.of("one", "two")
                .map(String::getBytes).collect(toList()));
        String txId1_0 = bob.invokes(i -> i.dApp(alice).function("checkMerkle",
                arg(tree1.rootHash()), arg(tree1.proofByLeafIndex(0).get()), arg("one".getBytes())))
                .getId().toString();
        String txId1_1 = bob.invokes(i -> i.dApp(alice).function("checkMerkle",
                arg(tree1.rootHash()), arg(tree1.proofByLeafIndex(1).get()), arg("two".getBytes())))
                .getId().toString();
        assertAll(
                () -> assertThat(alice.dataBool(txId1_0)).as("one").isTrue(),
                () -> assertThat(alice.dataBool(txId1_1)).as("two").isTrue()
        );


        Merkle tree2 = new Merkle(Stream.of("one", "two", "three", "four", "five")
                .map(String::getBytes).collect(toList()));
        String txId2_0 = bob.invokes(i -> i.dApp(alice).function("checkMerkle",
                arg(tree2.rootHash()), arg(tree2.proofByLeafIndex(3).get()), arg("four".getBytes())))
                .getId().toString();
        String txId2_1 = bob.invokes(i -> i.dApp(alice).function("checkMerkle",
                arg(tree2.rootHash()), arg(tree2.proofByLeafIndex(4).get()), arg("five".getBytes())))
                .getId().toString();
        assertAll(
                () -> assertThat(alice.dataBool(txId2_0)).as("four").isTrue(),
                () -> assertThat(alice.dataBool(txId2_1)).as("five").isTrue()
        );
    }

}
