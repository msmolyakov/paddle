package im.mak.paddle.util;

import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

public class ScriptUtil {

    public static String fromFile(String path) {
        try {
            return Files.toString(new File("src/test/resources/" + path), Charset.forName("UTF-8"));
        } catch(IOException e) {
            throw new Error(e);
        }
    }

}
