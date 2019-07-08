package im.mak.paddle.actions;

import im.mak.paddle.Account;
import im.mak.paddle.actions.exchange.Order;

import static im.mak.paddle.Constants.MIN_FEE;

public class Exchange implements Action {

    public Account sender;
    public Order buy;
    public Order sell;
    public long amount;
    public long price;
    public long buyMatcherFee;
    public long sellMatcherFee;
    public long fee;

    public Exchange(Account from) {
        this.sender = from;

        this.amount = 0;
        this.price = 0;
        this.buyMatcherFee = 0;
        this.sellMatcherFee = 0;
        this.fee = 0;
    }

    public static Exchange exchange(Account from) {
        return new Exchange(from);
    }

    public Exchange buy(Order buy) { //TODO how to put like action(from)?
        this.buy = buy;
        return this;
    }

    public Exchange sell(Order sell) {
        this.sell = sell;
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

    public Exchange fee(long fee) {
        this.fee = fee;
        return this;
    }

    public long calcAmount() {
        return amount > 0 ? amount : Math.min(buy.amount, sell.amount);
    }

    public long calcPrice() {
        return price > 0 ? price : buy.price;
    }

    public long calcBuyMatcherFee() {
        return buyMatcherFee > 0 ? buyMatcherFee : MIN_FEE * 3; //TODO proportionally from amount/price and order fee
    }

    public long calcSellMatcherFee() {
        return sellMatcherFee > 0 ? sellMatcherFee : MIN_FEE * 3;
    }

    @Override
    public long calcFee() {
        if (this.fee > 0) {
            return this.fee;
        } else {
            return buy.calcMatcherFee();
        }
    }

}
