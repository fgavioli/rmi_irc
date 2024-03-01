package client;

import java.nio.ByteBuffer;
import java.security.*;
import java.util.ArrayList;
import java.util.Random;

public class SignatureManager {
    private Signature signature;
    private KeyPair keys;

    public SignatureManager() throws NoSuchAlgorithmException, InvalidKeyException {
        // generate keypair and initialize signature object
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        keys = generator.generateKeyPair();
        signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(keys.getPrivate());
    }

    public byte[] sign(byte[] message) throws SignatureException, NoSuchAlgorithmException, InvalidKeyException {
        signature.update(message);
        return signature.sign();
    }

    public byte[] signWithNonce(byte[] message, byte[] nonce) throws SignatureException, NoSuchAlgorithmException, InvalidKeyException {
        nonce = new byte[8];
        new Random().nextBytes(nonce);
        byte[] fullMessage = new byte[message.length + nonce.length];
        ByteBuffer bb = ByteBuffer.wrap(fullMessage);
        bb.put(message);
        bb.put(nonce);
        return this.sign(bb.array());
    }

    public PublicKey getPublicKey() {
        return keys.getPublic();
    }
}
