package im.mak.paddle.settings;

public class SettingsTest {

    public static void main(String[] args) {
        System.out.println(new Settings().conf.getString("paddle.default-env"));
    }

}
