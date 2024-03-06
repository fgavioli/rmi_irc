package client;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.security.*;

public class IRCClient implements IRCClientInterface, Serializable {
    private final String username;

    public IRCClient(String username) throws NoSuchAlgorithmException, InvalidKeyException {
        this.username = username;

    }


    @Override
    public void sendMessage(String senderUsername, String message) throws RemoteException {
        System.out.println(senderUsername + " > " + message);
    }

    @Override
    public String getUsername() {
        return username;
    }

}
