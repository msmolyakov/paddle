package im.mak.paddle.actions.mass;

import im.mak.paddle.Account;

public class Transfer {

    public String recipient;
    public long amount;

    private Transfer(String recipient, long amount) {
        this.recipient = recipient;
        this.amount = amount;
    }

    public static Transfer to(String recipient, long amount) {
        return new Transfer(recipient, amount);
    }

    public static Transfer to(Account recipient, long amount) {
        return to(recipient.address(), amount);
    }

}
