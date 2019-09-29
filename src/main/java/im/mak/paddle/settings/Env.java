package im.mak.paddle.settings;

import com.typesafe.config.Config;

public class Env {

    private final String name;
    private final String apiUrl;
    private final char chainId;
    private final long blockInterval;
    private final String faucetSeed;

    public final Config _conf;

    public Env(String name, Config conf) {
        _conf = conf;

        this.name = name;
        apiUrl = _conf.getString("api-url");
        chainId = _conf.getString("chain-id").charAt(0);
        blockInterval = _conf.getDuration("block-interval").toMillis();
        faucetSeed = _conf.getString("faucet-seed");
    }

    public String name() {
        return name;
    }

    public String apiUrl() {
        return apiUrl;
    }

    public char chainId() {
        return chainId;
    }

    public long blockInterval() {
        return blockInterval;
    }

    public String faucetSeed() {
        return faucetSeed;
    }

    public String dockerImage() {
        return _conf.hasPath("docker-image") ? _conf.getString("docker-image") : null;
    }

    public boolean autoShutdown() {
        return !_conf.hasPath("auto-shutdown") || _conf.getBoolean("auto-shutdown");
    }

}
