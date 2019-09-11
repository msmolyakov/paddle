package im.mak.paddle.crypto;

import com.google.common.primitives.Bytes;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static com.wavesplatform.wavesj.Hash.blake2b;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

public class Merkle {

    private List<byte[]> hashes;
    private byte[] root;

    private byte[] findRoot(List<byte[]> nodes, boolean areNodes) {
        final AtomicInteger counter = new AtomicInteger();
        List<List<byte[]>> nodePairs = new ArrayList<>(
                nodes.stream()
                        .collect(groupingBy(it -> counter.getAndIncrement() / 2))
                        .values());

        List<byte[]> nextNodes = nodePairs.stream().map(n ->
                Bytes.concat(
                        new byte[]{(byte)(areNodes ? 1 : 0)},
                        n.get(0),
                        n.size() == 2 ? n.get(1) : new byte[]{}
                )
        ).collect(toList());

        if (nextNodes.size() == 1)
            return nextNodes.get(0);
        else
            return findRoot(nextNodes, true);
    }

    private byte[] leafHash(byte[] leaf) {
        return blake2b( Bytes.concat(new byte[]{0}, leaf), 0, leaf.length + 1 );
    }

    public Merkle(List<byte[]> leafs) {
        hashes = leafs.stream().map(this::leafHash).collect(toList());
        root = findRoot(hashes, false);
    }

    public Optional<byte[]> proofByLeafIndex(int i) {
        if (i < 0 || i >= hashes.size())
            return Optional.empty();

        return Optional.of();
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

}
