package im.mak.paddle;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.io.File;

public class Settings {

    public final String name;
    public final String apiUrl;
    public final char chainId;
    public final long blockInterval;
    public final String faucetSeed;
    public final String dockerImage;
    public final boolean autoShutdown;

    public final Config _conf;

    public Settings() {
        String base = "paddle";

        String rootPath = System.getProperty("user.dir") + "/" + base;

        Config overridden = ConfigFactory.parseFileAnySyntax(new File(rootPath))
                .withFallback(ConfigFactory.load(base))
                .withFallback(ConfigFactory.defaultReference());

        name = overridden.getString("paddle.env");
        _conf = overridden.getObject("paddle.envs." + name).toConfig();

        apiUrl = _conf.getString("api-url");
        chainId = _conf.getString("chain-id").charAt(0);
        blockInterval = _conf.getDuration("block-interval").toMillis();
        faucetSeed = _conf.getString("faucet-seed");
        dockerImage = _conf.hasPath("docker-image") ? _conf.getString("docker-image") : null;
        autoShutdown = !_conf.hasPath("auto-shutdown") || _conf.getBoolean("auto-shutdown");
    }

}
