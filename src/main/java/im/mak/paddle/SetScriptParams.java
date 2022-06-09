package im.mak.paddle;
import static im.mak.paddle.Node.node;

import com.wavesplatform.transactions.SetScriptTransaction;
import com.wavesplatform.transactions.common.Base64String;
import com.wavesplatform.wavesj.ScriptInfo;
import org.apache.maven.artifact.versioning.ComparableVersion;

import static im.mak.paddle.util.Constants.MIN_FEE;
import static im.mak.paddle.util.Constants.EXTRA_FEE;
import static im.mak.paddle.util.Constants.SENDER_FREE_COMPLEXITY;

public class SetScriptParams extends CommonParams<SetScriptParams> {

    protected Base64String compiledScript;

    protected SetScriptParams(Account sender) {
        super(sender, SetScriptTransaction.MIN_FEE);
    }

    @Override
    protected long getFee() {
        ComparableVersion version1_4 = new ComparableVersion("Waves v1.4");
        ComparableVersion nodeVersion = new ComparableVersion(node().getVersion());
        long totalWavesFee = MIN_FEE;
        if(nodeVersion.compareTo(version1_4) >= 0) {
            totalWavesFee *= (long) Math.ceil(this.compiledScript.bytes().length / 1024.0);
        }
        ScriptInfo scriptInfo = this.sender.getScriptInfo();
        if(scriptInfo.complexity() > SENDER_FREE_COMPLEXITY || scriptInfo.verifierComplexity() > SENDER_FREE_COMPLEXITY) {
            totalWavesFee += EXTRA_FEE;
        }
        return totalWavesFee;
    }

    protected SetScriptParams compiledScript(Base64String compiledScript) {
        this.compiledScript = compiledScript;
        return this;
    }

}