package im.mak.paddle.util;

import com.wavesplatform.transactions.account.Address;
import com.wavesplatform.transactions.common.Alias;
import com.wavesplatform.transactions.common.Recipient;

import static im.mak.paddle.Node.node;

public class RecipientResolver {

    public static Address toAddress(Recipient recipient) {
        return recipient.type() == 1 ? (Address) recipient : node().getAddressByAlias((Alias) recipient);
    }

}
