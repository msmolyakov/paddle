package im.mak.paddle.settings;

import java.net.MalformedURLException;
import java.net.URL;

public class NodeOptions {

    public URL apiUrl;
    public Character chainId;
    public String faucetSeed;
    public long blockInterval;
    public String dockerImage;
    public Boolean autoShutdown;

    public static NodeOptions options() {
        return new NodeOptions();
    }

    public NodeOptions() {
        apiUrl = null;
        chainId = null;
        faucetSeed = null;
        blockInterval = -1;
        dockerImage = null;
        autoShutdown = null;
    }

    public NodeOptions uri(String uri) {
        try {
            this.apiUrl = new URL(uri);
        } catch (MalformedURLException e) {
            throw new Error(e);
        }
        return this;
    }

    public NodeOptions chainId(char chainId) {
        this.chainId = chainId;
        return this;
    }

    public NodeOptions faucetSeed(String seed) {
        this.faucetSeed = seed;
        return this;
    }

    public NodeOptions blockInterval(long millis) {
        this.blockInterval = millis;
        return this;
    }

    public NodeOptions dockerImage(String name) {
        this.dockerImage = name;
        return this;
    }

    public NodeOptions autoShutdown(boolean enable) {
        this.autoShutdown = enable;
        return this;
    }

}
