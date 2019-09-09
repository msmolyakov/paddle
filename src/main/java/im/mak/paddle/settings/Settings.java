package im.mak.paddle.settings;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class Settings {

    public Config conf;

    public Settings() {
        System.out.println(System.getProperty("user.dir"));
        System.setProperty("config.file", System.getProperty("user.dir"));
        System.out.println(System.getProperty("config.file"));
        conf = ConfigFactory.load("paddle");
    }

}
