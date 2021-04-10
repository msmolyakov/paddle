package im.mak.paddle.params;

import im.mak.paddle.Account;
import com.wavesplatform.transactions.LeaseCancelTransaction;
import com.wavesplatform.transactions.common.Id;

public class LeaseCancelParams extends TxParams<LeaseCancelParams> {

    protected Id leaseId;

    public LeaseCancelParams(Account sender) {
        super(sender, LeaseCancelTransaction.MIN_FEE);
    }

    public LeaseCancelParams leaseId(Id leaseId) {
        this.leaseId = leaseId;
        return this;
    }

    public Id getLeaseId() {
        return this.leaseId;
    }

}
