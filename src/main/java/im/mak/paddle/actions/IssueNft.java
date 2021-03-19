package im.mak.paddle.actions;

import im.mak.paddle.Account;
import com.wavesplatform.transactions.IssueTransaction;
import com.wavesplatform.transactions.common.Base64String;

import java.util.Random;

import static im.mak.paddle.Node.node;

public class IssueNft extends Action<IssueNft> {

    public String name;
    public String description;
    public Base64String compiledScript;

    public IssueNft(Account sender) {
        super(sender, IssueTransaction.NFT_MIN_FEE);

        this.name = "NFT " + new Random().nextInt(100000);
        this.description = "";
    }

    public IssueNft name(String name) {
        this.name = name;
        return this;
    }

    public IssueNft description(String description) {
        this.description = description;
        return this;
    }

    public IssueNft script(String sourceCode) {
        this.compiledScript = sourceCode == null ? Base64String.empty() : node().compileScript(sourceCode).script();
        return this;
    }

}
