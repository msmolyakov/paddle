package im.mak.paddle.crypto;

public enum HashAlg {

    NOALG("NONE"),
    MD5("MD5"),
    SHA1("SHA1"),
    SHA224("SHA224"),
    SHA256("SHA256"),
    SHA384("SHA384"),
    SHA512("SHA512"),
    SHA3_224("SHA3-224"),
    SHA3_256("SHA3-256"),
    SHA3_384("SHA3-384"),
    SHA3_512("SHA3-512");

    private String value;

    HashAlg(String name) {
        this.value = name;
    }

    public String value() {
        return value;
    }
}
