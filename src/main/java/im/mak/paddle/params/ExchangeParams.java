package im.mak.paddle.params;

import im.mak.paddle.Account;
import com.wavesplatform.transactions.ExchangeTransaction;
import com.wavesplatform.transactions.exchange.Order;

public class ExchangeParams extends CommonParams<ExchangeParams> {

    protected Order order1;
    protected Order order2;
    protected long amount;
    protected long price;
    protected long buyMatcherFee;
    protected long sellMatcherFee;

    public ExchangeParams(Account sender) {
        super(sender, ExchangeTransaction.MIN_FEE);

        this.amount = 0;
        this.price = 0;
        this.buyMatcherFee = 0;
        this.sellMatcherFee = 0;
    }

    public ExchangeParams order1(Order order1) {
        this.order1 = order1;
        return this;
    }

    public ExchangeParams order2(Order order2) {
        this.order2 = order2;
        return this;
    }

    public ExchangeParams amount(long amount) {
        this.amount = amount;
        return this;
    }

    public ExchangeParams price(long price) {
        this.price = price;
        return this;
    }

    public ExchangeParams buyMatcherFee(long value) {
        this.buyMatcherFee = value;
        return this;
    }

    public ExchangeParams sellMatcherFee(long value) {
        this.sellMatcherFee = value;
        return this;
    }

}
