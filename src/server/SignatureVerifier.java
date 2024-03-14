package server;

import java.nio.ByteBuffer;
import java.security.*;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class SignatureVerifier {

    private ConcurrentHashMap<String, Signature> clientKeys = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, Random> clientRandomGenerators = new ConcurrentHashMap<>();

    SignatureVerifier() {}

    public int addSignature(String username, PublicKey publicKey) throws NoSuchAlgorithmException, InvalidKeyException {
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initVerify(publicKey);
        clientKeys.put(username, signature);
        int seed = new Random().nextInt();
        clientRandomGenerators.put(username, new Random(seed));
        return seed;
    }

    public void removeSignature(String username) {
        clientKeys.remove(username);
        clientRandomGenerators.remove(username);
    }

    public boolean verifySignatureWithoutNonce(String username, byte[] message, byte[] signedFingerprint) {
        try {
            clientKeys.get(username).update(message);
            return clientKeys.get(username).verify(signedFingerprint);
        } catch (SignatureException e) {
            return false;
        }
    }

    public boolean verifySignature(String username, byte[] message, byte[] signedFingerprint) {
        byte[] nonce = new byte[8];
        clientRandomGenerators.get(username).nextBytes(nonce);
        byte[] byteMessage = new byte[message.length + nonce.length];
        ByteBuffer bb = ByteBuffer.wrap(byteMessage);
        bb.put(message);
        bb.put(nonce);
        return verifySignatureWithoutNonce(username, bb.array(), signedFingerprint);
    }
}
