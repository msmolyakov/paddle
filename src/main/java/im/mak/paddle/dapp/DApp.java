package im.mak.paddle.dapp;

import com.wavesplatform.transactions.account.PrivateKey;
import com.wavesplatform.transactions.common.Base64String;
import im.mak.paddle.Account;

public abstract class DApp extends Account {

    public DApp(long initialBalance) {
        super(initialBalance);
    }

    public DApp(PrivateKey privateKey, long initialBalance) {
        super(privateKey, initialBalance);
    }

    public DApp(String seedPhrase, long initialBalance) {
        super(seedPhrase, initialBalance);
    }

    public DApp(long initialBalance, Base64String compiledScript) {
        this(initialBalance);
        setScript(compiledScript);
    }

    public DApp(PrivateKey privateKey, long initialBalance, Base64String compiledScript) {
        this(privateKey, initialBalance);
        setScript(compiledScript);
    }

    public DApp(String seedPhrase, long initialBalance, Base64String compiledScript) {
        this(seedPhrase, initialBalance);
        setScript(compiledScript);
    }

    public DApp(long initialBalance, String script) {
        this(initialBalance);
        setScript(script);
    }

    public DApp(long initialBalance, String script, boolean enableCompaction) {
        this(initialBalance);
        setScript(script, enableCompaction);
    }

    public DApp(PrivateKey privateKey, long initialBalance, String script) {
        this(privateKey, initialBalance);
        setScript(script);
    }

    public DApp(PrivateKey privateKey, long initialBalance, String script, boolean enableCompaction) {
        this(privateKey, initialBalance);
        setScript(script, enableCompaction);
    }

    public DApp(String seedPhrase, long initialBalance, String script) {
        this(seedPhrase, initialBalance);
        setScript(script);
    }

    public DApp(String seedPhrase, long initialBalance, String script, boolean enableCompaction) {
        this(seedPhrase, initialBalance);
        setScript(script, enableCompaction);
    }

}
