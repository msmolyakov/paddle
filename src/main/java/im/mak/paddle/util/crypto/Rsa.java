package im.mak.paddle.util.crypto;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.*;

public class Rsa {

    private BouncyCastleProvider bcp;
    private KeyPairGenerator gen;

    public KeyPair keys;

    public Rsa() {
        this.bcp = new BouncyCastleProvider();

        try {
            this.gen = KeyPairGenerator.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            throw new Error(e);
        }
        gen.initialize(2048, new SecureRandom());

        this.keys = gen.generateKeyPair();
    }

    public byte[] sign(HashAlg alg, byte[] source) {
        try {
            Signature sig = Signature.getInstance(alg.value() + "withRSA", bcp);
            sig.initSign(keys.getPrivate());
            sig.update(source);
            return sig.sign();
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            throw new Error(e);
        }
    }

}
