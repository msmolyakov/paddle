package im.mak.paddle.settings;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.io.File;

public class Settings {

    public final Config _conf;
    public final Env env;

    public Settings() {
        String base = "paddle";

        String rootPath = System.getProperty("user.dir") + "/" + base;
        System.out.println(rootPath); //TODO remove after jar check

        _conf = ConfigFactory.parseFileAnySyntax(new File(rootPath))
                .withFallback(ConfigFactory.load(base))
                .withFallback(ConfigFactory.defaultReference());
        //TODO check as dependency and from jar + local.conf
        //TODO env parameters?

        String envName = _conf.getString("paddle.env");
        env = new Env(envName, _conf.getObject("paddle.envs." + envName).toConfig());
    }

}
