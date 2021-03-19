package im.mak.paddle.actions;

import im.mak.paddle.Account;
import com.wavesplatform.transactions.ExchangeTransaction;
import com.wavesplatform.transactions.exchange.Order;
import com.wavesplatform.transactions.exchange.OrderType;

import static im.mak.paddle.Constants.EXTRA_FEE;
import static im.mak.paddle.Node.node;

public class Exchange extends Action<Exchange> {

    public Order order1;
    public Order order2;
    public long amount;
    public long price;
    public long buyMatcherFee;
    public long sellMatcherFee;

    public Exchange(Account sender) {
        super(sender, ExchangeTransaction.MIN_FEE);

        this.amount = 0;
        this.price = 0;
        this.buyMatcherFee = 0;
        this.sellMatcherFee = 0;
    }

    public Exchange order1(Order order1) {
        this.order1 = order1;
        return this;
    }

    public Exchange order2(Order order2) {
        this.order2 = order2;
        return this;
    }

    public Exchange amount(long amount) {
        this.amount = amount;
        return this;
    }

    public Exchange price(long price) {
        this.price = price;
        return this;
    }

    public Exchange buyMatcherFee(long value) {
        this.buyMatcherFee = value;
        return this;
    }

    public Exchange sellMatcherFee(long value) {
        this.sellMatcherFee = value;
        return this;
    }

    public long calcAmount() {
        return amount > 0 ? amount : Math.min(order1.amount().value(), order2.amount().value());
    }

    public long calcPrice() {
        return price > 0 ? price : order(OrderType.BUY).price().value();
    }

    private Order order(OrderType type) {
        if (order1.type() == type)
            return order1;
        else if (order2.type() == type)
            return order2;
        else throw new IllegalStateException("Can't find order with type \"" + type.value() + "\"");
    }

    @Override
    public long calcFee() {
        if (feeAmount > 0)
            return feeAmount;

        long totalWavesFee = super.calcFee();
        //TODO what about auto calc fee for orders?
        totalWavesFee += node().getAssetDetails(order1.amount().assetId()).isScripted() ? EXTRA_FEE : 0;
        totalWavesFee += node().getAssetDetails(order1.price().assetId()).isScripted() ? EXTRA_FEE : 0;

        return totalWavesFee;
    }

}
