package im.mak.paddle.settings;

public class SettingsTest {

    public static void main(String[] args) {
        Settings settings = new Settings();

        String env = settings._conf.getString("paddle.env");
        System.out.println(env);

        System.out.println(settings.env.name());
        System.out.println(settings.env.apiUrl());
        System.out.println(settings.env.chainId());
        System.out.println(settings.env.blockInterval());
        System.out.println(settings.env.richSeed());
    }

}
