package im.mak.paddle.params;

import im.mak.paddle.Account;
import com.wavesplatform.transactions.ExchangeTransaction;
import com.wavesplatform.transactions.exchange.Order;
import com.wavesplatform.transactions.exchange.OrderType;

import static im.mak.paddle.util.Constants.EXTRA_FEE;
import static im.mak.paddle.Node.node;

public class ExchangeParams extends TxParams<ExchangeParams> {

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

    public long calcAmount() {
        return amount > 0 ? amount : Math.min(order1.amount().value(), order2.amount().value());
    }

    public long calcPrice() {
        return price > 0 ? price : getOrder(OrderType.BUY).price().value();
    }

    public Order getOrder(OrderType type) {
        if (order1.type() == type)
            return order1;
        else if (order2.type() == type)
            return order2;
        else throw new IllegalStateException("Can't find order with type \"" + type.value() + "\"");
    }

    public Order getOrder1() {
        return this.order1;
    }

    public Order getOrder2() {
        return this.order2;
    }

    public long getAmount() {
        return this.amount;
    }

    public long getPrice() {
        return this.price;
    }

    public long getBuyMatcherFee() {
        return this.buyMatcherFee;
    }

    public long getSellMatcherFee() {
        return this.sellMatcherFee;
    }

    @Override
    public long getFee() {
        long totalWavesFee = super.getFee();

        //TODO what about auto calc fee for orders?
        totalWavesFee += node().getAssetDetails(order1.amount().assetId()).isScripted() ? EXTRA_FEE : 0;
        totalWavesFee += node().getAssetDetails(order1.price().assetId()).isScripted() ? EXTRA_FEE : 0;

        return totalWavesFee;
    }

}
