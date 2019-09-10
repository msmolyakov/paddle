package im.mak.paddle.util.crypto;

import com.google.common.primitives.Bytes;
import javafx.util.Pair;

import java.math.BigInteger;
import java.util.*;
import java.util.stream.IntStream;

import static com.wavesplatform.wavesj.Hash.blake2b;

public class Merkle {

    public static void testData() {
        Pair<String, String> pair = new Pair<>("", "");

        Set<byte[]> leafs = new HashSet<>();
        IntStream.range(0, 100).forEach(i -> leafs.add(
                BigInteger.valueOf(new Random().nextInt(10000)).toByteArray()
        ));

        List<byte[]> hashes = new ArrayList<>();
        leafs.forEach(l -> hashes.add(blake2b(Bytes.concat(new byte[]{0}, l), 0, l.length + 1)));

        //TODO topNode
        //TODO MerkleTree(topNode, hashes)
    }

    public static void main(String[] args) {
        byte[] source = "Hello".getBytes();

    }

}
