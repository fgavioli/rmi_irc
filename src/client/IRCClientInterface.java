package client;

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.PublicKey;

public interface IRCClientInterface extends Remote {
    String getUsername() throws RemoteException;
    void sendMessage(String senderUsername, String message) throws RemoteException;
    boolean requestPrivateChat(String username) throws IOException;
    void notifyLeave() throws RemoteException;
}
