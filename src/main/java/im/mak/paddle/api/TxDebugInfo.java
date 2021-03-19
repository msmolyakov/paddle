package im.mak.paddle.api;

import com.wavesplatform.wavesj.ApplicationStatus;
import com.wavesplatform.wavesj.StateChanges;
import com.wavesplatform.wavesj.TransactionDebugInfo;
import com.wavesplatform.transactions.InvokeScriptTransaction;
import com.wavesplatform.transactions.Transaction;

public class TxDebugInfo extends TransactionDebugInfo {

    public TxDebugInfo(Transaction tx, ApplicationStatus applicationStatus, int height, StateChanges stateChanges) {
        super(tx, applicationStatus, height, stateChanges);
    }

    @Override
    public InvokeScriptTransaction tx() {
        return (InvokeScriptTransaction) super.tx();
    }

}
