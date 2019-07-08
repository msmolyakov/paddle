package im.mak.paddle.actions;

import im.mak.paddle.Account;
import im.mak.paddle.exceptions.NodeError;

import java.io.IOException;
import java.nio.file.Path;

import static im.mak.paddle.Constants.EXTRA_FEE;
import static im.mak.paddle.Constants.ONE_WAVES;
import static java.nio.file.Files.readAllLines;

public class SetAssetScript implements Action {

    public Account sender;
    public String assetId;
    public String compiledScript;
    public long fee;

    public SetAssetScript(Account from) {
        this.sender = from;
        this.fee = 0;
    }

    public static SetAssetScript setAssetScript(Account from) {
        return new SetAssetScript(from);
    }

    public SetAssetScript asset(String assetId) {
        this.assetId = assetId;
        return this;
    }

    public SetAssetScript script(String sourceCode) {
        this.compiledScript = sourceCode == null ? null : this.sender.node.compileScript(sourceCode);
        return this;
    }

    public SetAssetScript script(Path filePath) {
        try {
            return script(String.join("\n", readAllLines(filePath)));
        } catch (IOException e) {
            throw new NodeError(e);
        }
    }

    public SetAssetScript fee(long fee) {
        this.fee = fee;
        return this;
    }

    @Override
    public long calcFee() {
        if (this.fee > 0) {
            return this.fee;
        } else {
            long totalFee = ONE_WAVES;
            totalFee += sender.isSmart() ? EXTRA_FEE : 0;
            totalFee += sender.node.isSmart(assetId) ? EXTRA_FEE : 0;
            return totalFee;
        }
    }

}
