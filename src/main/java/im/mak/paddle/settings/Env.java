package im.mak.paddle.settings;

import com.typesafe.config.Config;

public class Env {

    private final String name;
    private final String apiUrl;
    private final char chainId;
    private final long blockInterval;
    private final String richSeed;

    public Env(String name, Config conf) {
        this.name = name;
        apiUrl = conf.getString("api-url");
        chainId = conf.getString("chain-id").charAt(0);
        blockInterval = conf.getDuration("block-interval").toMillis();
        richSeed = conf.getString("rich-seed");
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

    public String richSeed() {
        return richSeed;
    }

}
