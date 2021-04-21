package im.mak.paddle.util;

import com.google.common.io.Files;
import com.wavesplatform.transactions.common.Base64String;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class Script {

    public static String fromFile(String path) {
        try {
            return Files.toString(new File(path), StandardCharsets.UTF_8);
        } catch(IOException e) {
            throw new Error(e);
        }
    }

    public static int getRideVersion(Base64String compiledScript) {
        return compiledScript.bytes()[2];
    }

    public static String setAssetType(String script) {
        String str = script.replaceAll("(\\{-#\\s*SCRIPT_TYPE\\s*)ACCOUNT(\\s*#-})", "$1ASSET$2");
        if (!str.matches("/(?s).*\\{-#\\s*SCRIPT_TYPE\\s+ACCOUNT\\s*#-}.*/gi"))
            str = "{-# SCRIPT_TYPE ASSET #-}\n" + str;
        return str;
    }

}
