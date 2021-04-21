package im.mak.paddle;

import com.wavesplatform.transactions.common.AssetId;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings({"unchecked", "unused"})
public class CommonParams<T extends CommonParams<?>> {

    protected final Account sender;
    protected final long baseFee;
    protected long additionalFee;
    protected AssetId feeAssetId;
    protected long timestamp;
    protected List<Account> signers;

    protected CommonParams(Account sender, long baseFee) {
        this.sender = sender;
        this.baseFee = baseFee;
        this.additionalFee = 0;
        this.feeAssetId = AssetId.WAVES;
        this.timestamp = 0;
        this.signers = new ArrayList<>();
    }

    protected long getFee() {
        return baseFee + sender.getScriptInfo().extraFee() + additionalFee;
    }

    public T timestamp(long timestamp) {
        this.timestamp = timestamp;
        return (T) this;
    }

    public T additionalFee(long amount) {
        this.additionalFee = amount;
        return (T) this;
    }

    public T signedBy(Account... signers) {
        this.signers.addAll(Arrays.asList(signers));
        return (T) this;
    }

    public T signedBy(int proofIndex, Account signer) {
        for (int i = signers.size(); i <= proofIndex; i++)
            signers.add(null);
        this.signers.set(proofIndex, signer);
        return (T) this;
    }

}
