package im.mak.paddle.api;

import com.wavesplatform.wavesj.ApplicationStatus;
import com.wavesplatform.wavesj.TransactionInfo;
import com.wavesplatform.transactions.Transaction;

public class TxInfo<T extends Transaction> extends TransactionInfo {

    public TxInfo(Transaction tx, ApplicationStatus applicationStatus, int height) {
        super(tx, applicationStatus, height);
    }

    @Override
    public T tx() {
        return (T) super.tx();
    }

}
