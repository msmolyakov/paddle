package im.mak.paddle.actions.mass;

import im.mak.paddle.Account;

public class Recipient {

    public String recipient;
    public long amount;

    private Recipient(String recipient, long amount) {
        this.recipient = recipient;
        this.amount = amount;
    }

    public static Recipient to(String recipient, long amount) {
        return new Recipient(recipient, amount);
    }

    public static Recipient to(Account recipient, long amount) {
        return to(recipient.address(), amount);
    }

}
