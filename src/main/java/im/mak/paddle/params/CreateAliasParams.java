package im.mak.paddle.params;

import im.mak.paddle.Account;
import com.wavesplatform.transactions.CreateAliasTransaction;
import com.wavesplatform.transactions.common.Alias;

public class CreateAliasParams extends CommonParams<CreateAliasParams> {

    protected Alias alias;

    public CreateAliasParams(Account sender) {
        super(sender, CreateAliasTransaction.MIN_FEE);
    }

    public CreateAliasParams alias(Alias alias) {
        this.alias = alias;
        return this;
    }

    public CreateAliasParams alias(String alias) {
        return alias(Alias.as(alias));
    }

}
