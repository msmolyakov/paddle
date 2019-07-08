package im.mak.paddle.util;

import java.nio.file.Path;
import java.nio.file.Paths;

public class PathUtil {

    public static Path path(String path) {
        return Paths.get("src/test/resources", path);
    }

}
