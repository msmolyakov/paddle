package im.mak.paddle.actions;

import im.mak.paddle.Account;
import com.wavesplatform.transactions.LeaseTransaction;
import com.wavesplatform.transactions.common.Recipient;

public class Lease extends Action<Lease> {

    public Recipient recipient;
    public long amount;

    public Lease(Account sender) {
        super(sender, LeaseTransaction.MIN_FEE);
    }

    public Lease to(Recipient recipient) {
        this.recipient = recipient;
        return this;
    }

    public Lease to(Account account) {
        return to(account.address());
    }

    public Lease amount(long amount) {
        this.amount = amount;
        return this;
    }

}
