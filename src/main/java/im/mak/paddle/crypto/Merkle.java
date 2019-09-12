package im.mak.paddle.crypto;

import com.google.common.primitives.Bytes;
import com.wavesplatform.wavesj.Base58;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.wavesplatform.wavesj.Hash.blake2b;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

public class Merkle {

    private List<byte[]> hashes;
    private List<byte[]> proofs;
    private byte[] root;

    private List<byte[]> initProofs(List<byte[]> leafsHashes) {
        List<byte[]> result = new ArrayList<>();
        for (int i = 0; i < leafsHashes.size(); i += 2) {
            if (i + 1 < leafsHashes.size()) {
                //TODO флаг side наверно байт, а не массив
                result.add(Bytes.concat(new byte[]{1}, leafsHashes.get(i + 1)));
                result.add(Bytes.concat(new byte[]{0}, leafsHashes.get(i)));
            } else {
                result.add(Bytes.concat(new byte[]{1}, new byte[]{}));
            }
        }
        return result;
    }

    private void increaseProofs(List<byte[]> proofs, List<byte[]> nodes) {
        int ratio = (proofs.size() + 1) / nodes.size();
        for (int n = 0; n < nodes.size(); n += 2) {
            if (n + 1 < nodes.size()) {
                for (int p = 0; p < ratio; p++) {
                    int l = n * ratio + p;
                    int r = (n + 1) * ratio + p;
                    if (l < proofs.size()) {
                        //TODO флаг side наверно байт, а не массив
                        proofs.set(l, Bytes.concat(proofs.get(l), new byte[]{1}, nodes.get(n + 1)));
                        if (r < proofs.size())
                            proofs.set(r, Bytes.concat(proofs.get(r), new byte[]{0}, nodes.get(n)));
                    } else break;
                }
            } else {
                for (int p = 0; p < ratio; p++) {
                    int l = n * ratio + p;
                    if (l < proofs.size()) {
                        proofs.set(l, Bytes.concat(proofs.get(l), new byte[]{1}, new byte[]{}));
                    } else break;
                }
            }
        }
    }

    private byte[] findRoot(List<byte[]> nodes, boolean areNodes) {
        final AtomicInteger counter = new AtomicInteger();
        List<List<byte[]>> nodePairs = new ArrayList<>(
                nodes.stream()
                        .collect(groupingBy(it -> counter.getAndIncrement() / 2))
                        .values());

        List<byte[]> nextNodes = nodePairs.stream().map(n ->
                Bytes.concat(
                        new byte[]{(byte)(areNodes ? 1 : 0)}, //TODO флаг node наверно байт, а не массив
                        n.get(0),
                        n.size() == 2 ? n.get(1) : new byte[]{}
                )
        ).collect(toList());

        if (nextNodes.size() == 1)
            return nextNodes.get(0);
        else {
            increaseProofs(proofs, nextNodes);
            return findRoot(nextNodes, true);
        }
    }

    private byte[] fastHash(byte[] source) {
        return blake2b(source, 0, source.length);
    }

    private byte[] leafHash(byte[] source) {
        //TODO флаг node наверно байт, а не массив
        return fastHash(Bytes.concat(new byte[]{0}, source));
    }

    public Merkle(List<byte[]> leafs) {
        hashes = leafs.stream().map(this::leafHash).collect(toList());
        proofs = initProofs(hashes);
        root = findRoot(hashes, false);
    }

    public byte[] rootHash() {
        return this.root.clone(); //TODO immutable getters here and below? clone()?
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
        Optional<byte[]> actual = proofByLeaf(leafValue);
        /*if (actual.isPresent())
            return Optional.of(Arrays.equals(proof, actual.get()));
        else return Optional.empty();*/
        return actual.map(bytes -> Arrays.equals(proof, bytes)); //TODO from IntelliJ suggestion
    }

    //TODO add leaf?
    //TODO remove leaf?

    public static void main(String[] args) {
        List<byte[]> data = Stream.of("one", "two", "three", "four", "five")
                .map(String::getBytes).collect(toList());

        Merkle tree = new Merkle(data);

        String proof0 = Base58.encode(tree.proofByLeafIndex(0).get());
        String proof1 = Base58.encode(tree.proofByLeafIndex(1).get());
        String proof2 = Base58.encode(tree.proofByLeafIndex(2).get());
        String proof3 = Base58.encode(tree.proofByLeafIndex(3).get());
        String proof4 = Base58.encode(tree.proofByLeafIndex(4).get());

        System.out.println();
    }

}
