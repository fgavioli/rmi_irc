package server;

import java.nio.ByteBuffer;
import java.security.*;
import java.util.HashMap;

public class SignatureVerifier {

    private HashMap<String, Signature> clientKeys = new HashMap<>();

    SignatureVerifier() {

    }

    public void addSignature(String username, PublicKey publicKey) throws NoSuchAlgorithmException, InvalidKeyException {
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initVerify(publicKey);
        clientKeys.put(username, signature);
    }

    public void removeSignature(String username) {
        clientKeys.remove(username);
    }

    public boolean verifySignature(String username, byte[] message, byte[] nonce, byte[] signedFingerprint) {
        if (nonce == null)
            nonce = new byte[0];
        byte[] byteMessage = new byte[message.length + nonce.length];
        ByteBuffer bb = ByteBuffer.wrap(byteMessage);
        bb.put(message);
        bb.put(nonce);
        try {
            clientKeys.get(username).update(bb.array());
            return clientKeys.get(username).verify(signedFingerprint);
        } catch (SignatureException e) {
            return false;
        }
    }
}
