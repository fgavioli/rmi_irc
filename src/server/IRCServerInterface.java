package server;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.PublicKey;
import java.security.SignatureException;
import java.util.ArrayList;

public interface IRCServerInterface extends Remote {
    int connect(String username, PublicKey publicKey, byte[] signedFingerprint) throws RemoteException;
    String getGreeting() throws RemoteException;

    ArrayList<String> getUsers() throws RemoteException;
    ArrayList<String> getChannels() throws RemoteException;
    int joinChannel(String username, String channelName, byte[] nonce, byte[] signedFingerprint) throws RemoteException;
    int joinPrivateChat(String username, String targetUsername, byte[] nonce, byte[] signedFingerprint) throws RemoteException, SignatureException;
}
