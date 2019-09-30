package im.mak.paddle.crypto;

import im.mak.paddle.Account;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.stream.Stream;

import static im.mak.paddle.Async.async;
import static im.mak.paddle.actions.invoke.Arg.arg;
import static im.mak.paddle.util.ScriptUtil.fromFile;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class MerkleTreeTest {

    private Account alice, bob;

    @BeforeEach
    void before() {
        async(
                () -> alice = new Account(1_00000000),
                () -> bob = new Account(1_00000000)
        );
        alice.setsScript(s -> s.script(fromFile("merkle.ride")));
    }

    @Test
    void merkle() {
        MerkleTree tree0 = new MerkleTree(Stream.of("one")
                .map(String::getBytes).collect(toList()));
        String txId0 = bob.invokes(i -> i.dApp(alice).function("checkMerkle",
                arg(tree0.rootHash()), arg(tree0.proofByLeafIndex(0).get()), arg("one".getBytes())))
                .getId().toString();
        assertThat(alice.dataBool(txId0)).isTrue();


        MerkleTree tree1 = new MerkleTree(Stream.of("one", "two")
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


        MerkleTree tree2 = new MerkleTree(Stream.of("one", "two", "three", "four", "five", "six", "seven", "eight", "nine")
                .map(String::getBytes).collect(toList()));
        String txId2_0 = bob.invokes(i -> i.dApp(alice).function("checkMerkle",
                arg(tree2.rootHash()), arg(tree2.proofByLeafIndex(0).get()), arg("one".getBytes())))
                .getId().toString();
        String txId2_1 = bob.invokes(i -> i.dApp(alice).function("checkMerkle",
                arg(tree2.rootHash()), arg(tree2.proofByLeafIndex(1).get()), arg("two".getBytes())))
                .getId().toString();
        String txId2_2 = bob.invokes(i -> i.dApp(alice).function("checkMerkle",
                arg(tree2.rootHash()), arg(tree2.proofByLeafIndex(2).get()), arg("three".getBytes())))
                .getId().toString();
        String txId2_3 = bob.invokes(i -> i.dApp(alice).function("checkMerkle",
                arg(tree2.rootHash()), arg(tree2.proofByLeafIndex(3).get()), arg("four".getBytes())))
                .getId().toString();
        String txId2_4 = bob.invokes(i -> i.dApp(alice).function("checkMerkle",
                arg(tree2.rootHash()), arg(tree2.proofByLeafIndex(4).get()), arg("five".getBytes())))
                .getId().toString();
        String txId2_5 = bob.invokes(i -> i.dApp(alice).function("checkMerkle",
                arg(tree2.rootHash()), arg(tree2.proofByLeafIndex(5).get()), arg("six".getBytes())))
                .getId().toString();
        String txId2_6 = bob.invokes(i -> i.dApp(alice).function("checkMerkle",
                arg(tree2.rootHash()), arg(tree2.proofByLeafIndex(6).get()), arg("seven".getBytes())))
                .getId().toString();
        String txId2_7 = bob.invokes(i -> i.dApp(alice).function("checkMerkle",
                arg(tree2.rootHash()), arg(tree2.proofByLeafIndex(7).get()), arg("eight".getBytes())))
                .getId().toString();
        String txId2_8 = bob.invokes(i -> i.dApp(alice).function("checkMerkle",
                arg(tree2.rootHash()), arg(tree2.proofByLeafIndex(8).get()), arg("nine".getBytes())))
                .getId().toString();

        assertAll(
                () -> assertThat(alice.dataBool(txId2_0)).as("one").isTrue(),
                () -> assertThat(alice.dataBool(txId2_1)).as("two").isTrue(),
                () -> assertThat(alice.dataBool(txId2_2)).as("three").isTrue(),
                () -> assertThat(alice.dataBool(txId2_3)).as("four").isTrue(),
                () -> assertThat(alice.dataBool(txId2_4)).as("five").isTrue(),
                () -> assertThat(alice.dataBool(txId2_5)).as("six").isTrue(),
                () -> assertThat(alice.dataBool(txId2_6)).as("seven").isTrue(),
                () -> assertThat(alice.dataBool(txId2_7)).as("eight").isTrue(),
                () -> assertThat(alice.dataBool(txId2_8)).as("nine").isTrue()
        );
    }

}
