package im.mak.paddle.crypto;

import com.google.common.primitives.Bytes;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static com.wavesplatform.wavesj.Hash.blake2b;
import static java.lang.Math.*;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

@SuppressWarnings({"WeakerAccess", "unused"})
public class MerkleTree {

    private final List<byte[]> hashes;
    private final List<byte[]> proofs;
    private final byte[] root;

    private byte LEFT = 0, RIGHT = 1;
    private byte[] LEAF = new byte[]{0}, NODE = new byte[]{1};
    private byte[] EMPTY = new byte[]{};
    private byte[] EMPTY_PROOF = new byte[]{LEFT, 0};

    private int sizeWithEmpty(List list) {
        return max((int) pow(2, ceil(log(list.size()) / log(2))), 2);
    }

    private byte[] proof(byte side, byte[] hash) {
        return Bytes.concat(new byte[]{side, (byte) hash.length}, hash);
    }

    private List<byte[]> initProofs(List<byte[]> leafsHashes) {
        List<byte[]> result = new ArrayList<>();
        for (int i = 0; i < leafsHashes.size(); i += 2) {
            if (i + 1 < leafsHashes.size()) {
                result.add(proof(LEFT, leafsHashes.get(i + 1)));
                result.add(proof(RIGHT, leafsHashes.get(i)));
            } else result.add(EMPTY_PROOF);
        }
        return result;
    }

    private void increaseProofs(List<byte[]> proofs, List<byte[]> nodes) {
        int ratio = sizeWithEmpty(proofs) / sizeWithEmpty(nodes);
        for (int n = 0; n < nodes.size(); n += 2) {
            for (int p = 0; p < ratio; p++) {
                int l = n * ratio + p;
                int r = (n + 1) * ratio + p;
                if (l < proofs.size()) {
                    if (n + 1 < nodes.size()) {
                        byte[] rightNode = nodes.get(n + 1);
                        proofs.set(l, Bytes.concat(proofs.get(l), proof(LEFT, rightNode)));
                        if (r < proofs.size()) {
                            byte[] leftNode = nodes.get(n);
                            proofs.set(r, Bytes.concat(proofs.get(r), proof(RIGHT, leftNode)));
                        }
                    } else {
                        proofs.set(l, Bytes.concat(proofs.get(l), EMPTY_PROOF));
                    }
                } else break;
            }
        }
    }

    private byte[] findRoot(List<byte[]> nodes) {
        final AtomicInteger counter = new AtomicInteger();
        List<List<byte[]>> nodePairs = new ArrayList<>(
                nodes.stream()
                        .collect(groupingBy(it -> counter.getAndIncrement() / 2))
                        .values());

        List<byte[]> nextNodes = nodePairs.stream().map(n ->
                fastHash(Bytes.concat(NODE, n.get(0), n.size() == 2 ? n.get(1) : EMPTY))
        ).collect(toList());

        if (nextNodes.size() == 1)
            return nextNodes.get(0);
        else {
            increaseProofs(proofs, nextNodes);
            return findRoot(nextNodes);
        }
    }

    private byte[] fastHash(byte[] source) {
        return blake2b(source, 0, source.length);
    }

    private byte[] leafHash(byte[] source) {
        return fastHash(Bytes.concat(LEAF, source));
    }

    public MerkleTree(List<byte[]> leafs) {
        hashes = leafs.stream().map(this::leafHash).collect(toList());
        proofs = initProofs(hashes);
        root = findRoot(hashes);
    }

    public byte[] rootHash() {
        return this.root;
    }

    public Optional<byte[]> proofByLeafIndex(int i) {
        if (i < 0 || i >= hashes.size())
            return Optional.empty();
        return Optional.of(proofs.get(i));
    }

    public Optional<byte[]> proofByLeafHash(byte[] hash) {
        OptionalInt index = IntStream.range(0, hashes.size())
                .filter(i -> Arrays.equals(hash, hashes.get(i)))
                .findFirst();
        return index.isPresent() ? proofByLeafIndex(index.getAsInt()) : Optional.empty();
    }

    public Optional<byte[]> proofByLeaf(byte[] leaf) {
        return proofByLeafHash(leafHash(leaf));
    }

    public Optional<Boolean> isProofValid(byte[] proof, byte[] leafValue) {
        return proofByLeaf(leafValue).map(bytes -> Arrays.equals(proof, bytes));
    }

}
