package im.mak.paddle.actions;

import im.mak.paddle.Account;
import com.wavesplatform.transactions.LeaseCancelTransaction;
import com.wavesplatform.transactions.common.Id;

public class LeaseCancel extends Action<LeaseCancel> {

    public Id leaseId;

    public LeaseCancel(Account sender) {
        super(sender, LeaseCancelTransaction.MIN_FEE);
    }

    public LeaseCancel leaseId(Id leaseId) {
        this.leaseId = leaseId;
        return this;
    }

}
