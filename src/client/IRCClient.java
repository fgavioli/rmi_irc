package client;

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class IRCClient extends UnicastRemoteObject implements IRCClientInterface {
    private String username;
    private ClientSession session;
    private boolean requestedPrivateChat = false;
    private String requestedPrivateChatUsername = "";
    private boolean notifiedLeaveChannel = false;

    /**
     * IRCClient constructor
     * @param username the client's chosen username
     * @param session the session to which this client is related
     */
    public IRCClient(String username, ClientSession session) throws RemoteException {
        super();
        this.username = username;
        this.session = session;
    }

    /**
     * Returns true if some user has requested a private chat
     * @return true if some other user has requested a private chat, false otherwise
     */
    public boolean hasRequestedPrivateChat() {
        return requestedPrivateChat;
    }


    /**
     * Sends a message to the client
     * @param senderUsername the sender of the message
     * @param message the message
     */
    @Override
    public void sendMessage(String senderUsername, String message) throws RemoteException {
        System.out.println(senderUsername + " > " + message);
    }

    /**
     * Requests a private chat with the client
     * @param username the username that requested the private chat
     * @return true if the client accepted the chat, false if the client rejected the chat
     */
    @Override
    public boolean requestPrivateChat(String username) throws IOException {
        // grab scanner lock before printing
        session.lockStdin();
        System.out.print("You have received a request to join a private chat by " + username + ".\nDo you want to accept it? [y/N]: ");
        boolean response;
        String yesOrNo = session.getScanner().readLine();
        response =  switch (yesOrNo.toLowerCase()) {
            case "y", "ye", "yes", "ya", "ys", "yeah" -> true;
            default -> false;
        };
        // raise interrupt
        requestedPrivateChatUsername = username;
        requestedPrivateChat = response;
        session.unlockStdin();
        return response;
    }

    /**
     * Notifies the client that the channel he's into is closing
     */
    @Override
    public void notifyLeave() throws RemoteException {
        notifiedLeaveChannel = true;
    }

    /**
     * Returns the client's username
     * @return the username
     */
    @Override
    public String getUsername() {
        return username;
    }

    /**
     * Returns the username that requested the private chat
     * @return the username
     */
    public String getTargetUsername() {
        return requestedPrivateChatUsername;
    }

    /**
     * Invoke this after entering a private chat
     */
    public void resetPrivateChatData() {
        requestedPrivateChat = false;
        requestedPrivateChatUsername = "";
    }

    /**
     * Returns whether the client received a channel closing notification
     * @return true if the client received a channel closing notification, false otherwise
     */
    public boolean recievedNotifyLeave() {
        return notifiedLeaveChannel;
    }

    /**
     * Invoke this after leaving a private chat
     */
    public void resetNotifyLeave() {
        notifiedLeaveChannel = false;
    }
}
