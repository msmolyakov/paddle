package im.mak.paddle.actions;

import im.mak.paddle.Account;
import im.mak.waves.transactions.CreateAliasTransaction;
import im.mak.waves.transactions.common.Alias;

public class CreateAlias extends Action<CreateAlias> {

    public Alias alias;

    public CreateAlias(Account sender) {
        super(sender, CreateAliasTransaction.MIN_FEE);
    }

    public CreateAlias alias(Alias alias) {
        this.alias = alias;
        return this;
    }

    public CreateAlias alias(String alias) {
        return alias(Alias.as(alias));
    }

}
