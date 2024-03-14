package server;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.PublicKey;
import java.util.ArrayList;

public interface IRCServerInterface extends Remote {
    int connect(String username, PublicKey publicKey, byte[] signedFingerprint) throws RemoteException;
    String getGreeting() throws RemoteException;
    ArrayList<String> getUsers() throws RemoteException;
    ArrayList<String> getChannelDescriptions() throws RemoteException;
    int joinChannel(String username, String channelName, byte[] signedFingerprint) throws RemoteException;
    void leaveChannel(String username, String channelName, byte[] signedFingerprint) throws RemoteException;
    int joinPrivateChat(String username, String targetUsername, byte[] signedFingerprint) throws RemoteException;
    void disconnect(String username, byte[] signedFingerprint) throws RemoteException;
    void sendMessage(String username, String channel, String message, byte[] signedFingerprint) throws RemoteException;
}