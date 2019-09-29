package im.mak.paddle.settings;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.io.File;

public class PaddleSettings {

    public final Config _conf;
    public final Env env;

    public PaddleSettings() {
        String base = "paddle";

        String rootPath = System.getProperty("user.dir") + "/" + base;
        System.out.println(rootPath);

        _conf = ConfigFactory.parseFileAnySyntax(new File(rootPath))
                .withFallback(ConfigFactory.load(base))
                .withFallback(ConfigFactory.defaultReference());

        String envName = _conf.getString("paddle.env");
        env = new Env(envName, _conf.getObject("paddle.envs." + envName).toConfig());
    }

}
