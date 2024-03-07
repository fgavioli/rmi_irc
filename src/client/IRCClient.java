package client;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.*;
import java.util.Collection;
import java.util.Scanner;

public class IRCClient extends UnicastRemoteObject implements IRCClientInterface {
    private final String username;

    public IRCClient(String username) throws RemoteException {
        super();
        this.username = username;
    }


    @Override
    public void sendMessage(String senderUsername, String message) throws RemoteException {
        System.out.println(senderUsername + " > " + message);
    }

    @Override
    public boolean requestPrivateChat(String username) throws RemoteException {
        System.out.println("You have received a request to join a private chat by " + username + ".\nDo you want to accept it? [y/N]: ");
        try (Scanner s = new Scanner(System.in)) {
            return switch (s.nextLine()) {
                case "y", "Y", "Yes", "yes" -> true;
                default -> false;
            };
        }
    }

    @Override
    public String getUsername() {
        return username;
    }

}
