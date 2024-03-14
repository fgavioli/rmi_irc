package client;

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IRCClientInterface extends Remote {

    /**
     * Returns the client's username
     * @return the username
     */
    String getUsername() throws RemoteException;

    /**
     * Sends a message to the client
     * @param senderUsername the sender of the message
     * @param message the message
     */
    void sendMessage(String senderUsername, String message) throws RemoteException;

    /**
     * Requests a private chat with the client
     * @param username the username that requested the private chat
     * @return true if the client accepted the chat, false if the client rejected the chat
     */
    boolean requestPrivateChat(String username) throws IOException;

    /**
     * Notifies the client that the channel he's into is closing
     */
    void notifyLeave() throws RemoteException;
}
