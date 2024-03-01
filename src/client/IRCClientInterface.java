package client;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.PublicKey;

public interface IRCClientInterface extends Remote {
    String getUsername() throws RemoteException;
}
