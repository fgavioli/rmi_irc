package client;

import java.nio.ByteBuffer;
import java.security.*;
import java.util.Random;

public class SignatureManager {
    private Signature signature;
    private KeyPair keys;
    private Random randomGenerator;

    /**
     * SignatureManager constructor
     */
    public SignatureManager() throws NoSuchAlgorithmException, InvalidKeyException {
        // generate keypair and initialize signature object
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        keys = generator.generateKeyPair();
        signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(keys.getPrivate());
    }

    /**
     * Signs a message.
     * @param message the message to be signed
     * @return the signed message
     */
    public byte[] sign(byte[] message) throws SignatureException, NoSuchAlgorithmException, InvalidKeyException {
        signature.update(message);
        return signature.sign();
    }

    /**
     * Signs a message with an appended nonce
     * @param message the message to be signed
     * @return the signed message
     */
    public byte[] signWithNonce(byte[] message) throws SignatureException, NoSuchAlgorithmException, InvalidKeyException {
        byte[] nonce = new byte[8];
        randomGenerator.nextBytes(nonce);
        byte[] fullMessage = new byte[message.length + nonce.length];
        ByteBuffer bb = ByteBuffer.wrap(fullMessage);
        bb.put(message);
        bb.put(nonce);
        return this.sign(bb.array());
    }

    /**
     * Returns the public key associated with this manager
     * @return the public key
     */
    public PublicKey getPublicKey() {
        return keys.getPublic();
    }

    /**
     * Initializes the nonce generator with the specified seed
     * @param seed the seed to initialize the nonce generator with
     */
    public void setSeed(int seed) {
        randomGenerator = new Random(seed);
    }
}
