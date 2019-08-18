package im.mak.paddle.util;

public class ScriptUtil {

    public static String fromFile(String path) {
        return Script.fromFile("src/test/resources/" + path);
    }

}
