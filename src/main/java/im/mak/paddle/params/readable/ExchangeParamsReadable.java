package im.mak.paddle.params.readable;

import com.wavesplatform.transactions.common.AssetId;
import com.wavesplatform.transactions.exchange.Order;
import com.wavesplatform.transactions.exchange.OrderType;
import im.mak.paddle.Account;
import im.mak.paddle.params.ExchangeParams;

import java.util.List;

import static im.mak.paddle.Node.node;
import static im.mak.paddle.util.Constants.EXTRA_FEE;

public class ExchangeParamsReadable extends ExchangeParams {

    public ExchangeParamsReadable(Account sender) {
        super(sender);
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

    /* COMMON PARAMS */

    @Override
    public Account getSender() {
        return super.getSender();
    }

    @Override
    public long getTimestamp() {
        return super.getTimestamp();
    }

    @Override
    public AssetId getFeeAssetId() {
        return super.getFeeAssetId();
    }

    @Override
    public List<Object> getSignersAndProofs() {
        return super.getSignersAndProofs();
    }

}
