package im.mak.paddle.params;

import com.wavesplatform.crypto.Bytes;
import com.wavesplatform.transactions.account.PrivateKey;
import com.wavesplatform.transactions.common.AssetId;
import com.wavesplatform.transactions.common.Proof;
import im.mak.paddle.Account;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.Collections.singletonList;

@SuppressWarnings({"unchecked", "unused", "rawtypes"})
public abstract class TxParams<T extends TxParams> {

    protected final Account sender;
    protected long timestamp;
    protected final long baseFee;
    protected AssetId feeAssetId;
    protected long additionalFee;
    protected List<Object> signersAndProofs;

    protected TxParams(Account sender, long baseFee) {
        this.sender = sender;
        this.baseFee = baseFee;
        this.signersAndProofs = new ArrayList<>(singletonList(sender.privateKey()));
    }

    public T timestamp(long timestamp) {
        this.timestamp = timestamp;
        return (T) this;
    }

    public T additionalFee(long amount) {
        this.additionalFee = amount;
        return (T) this;
    }

    public T addSigners(PrivateKey... signers) {
        this.signersAndProofs.addAll(Arrays.asList(signers));
        return (T) this;
    }

    public T addSigners(Account... signers) {
        PrivateKey[] keys = Arrays.stream(signers).map(Account::privateKey).toArray(PrivateKey[]::new);
        return addSigners(keys);
    }

    public T setSigner(int proofIndex, PrivateKey signer) {
        for (int i = signersAndProofs.size(); i <= proofIndex; i++)
            signersAndProofs.add(null);
        this.signersAndProofs.set(proofIndex, signer);
        return (T) this;
    }

    public T setSigner(int proofIndex, Account signer) {
        return setSigner(proofIndex, signer.privateKey());
    }

    public T addProofs(Proof... proofs) {
        this.signersAndProofs.addAll(Arrays.asList(proofs));
        return (T) this;
    }

    public T setProof(int proofIndex, Proof proof) {
        for (int i = signersAndProofs.size(); i <= proofIndex; i++)
            signersAndProofs.add(Proof.as(Bytes.empty()));
        this.signersAndProofs.set(proofIndex, proof);
        return (T) this;
    }

    public Account getSender() {
        return this.sender;
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    public long getFee() {
        return baseFee + sender.getScriptInfo().extraFee() + additionalFee;
    }

    public AssetId getFeeAssetId() {
        return this.feeAssetId;
    }

    public List<Object> getSignersAndProofs() {
        return this.signersAndProofs;
    }

}
