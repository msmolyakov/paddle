package im.mak.paddle.params;

import im.mak.paddle.Account;
import com.wavesplatform.transactions.LeaseTransaction;
import com.wavesplatform.transactions.common.Recipient;

public class LeaseParams extends TxParams<LeaseParams> {

    protected Recipient recipient;
    protected long amount;

    public LeaseParams(Account sender) {
        super(sender, LeaseTransaction.MIN_FEE);
    }

    public LeaseParams to(Recipient recipient) {
        this.recipient = recipient;
        return this;
    }

    public LeaseParams to(Account account) {
        return to(account.address());
    }

    public LeaseParams amount(long amount) {
        this.amount = amount;
        return this;
    }

    public Recipient getRecipient() {
        return this.recipient;
    }

    public long getAmount() {
        return this.amount;
    }

}
