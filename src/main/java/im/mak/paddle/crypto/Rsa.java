package im.mak.paddle.crypto;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.*;

public class Rsa {

    private final BouncyCastleProvider bcp;
    private final KeyPair keys;

    public Rsa() {
        this.bcp = new BouncyCastleProvider();

        KeyPairGenerator gen;
        try {
            gen = KeyPairGenerator.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            throw new Error(e);
        }
        gen.initialize(2048, new SecureRandom());

        this.keys = gen.generateKeyPair();
    }

    public byte[] privateKey() {
        return keys.getPrivate().getEncoded();
    }

    public byte[] publicKey() {
        return keys.getPublic().getEncoded();
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
