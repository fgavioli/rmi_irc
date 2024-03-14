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

    public IRCClient(String username, ClientSession session) throws RemoteException {
        super();
        this.username = username;
        this.session = session;
    }

    public boolean hasRequestedPrivateChat() {
        return requestedPrivateChat;
    }


    @Override
    public void sendMessage(String senderUsername, String message) throws RemoteException {
        System.out.println(senderUsername + " > " + message);
    }

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

    @Override
    public void notifyLeave() throws RemoteException {
        System.out.println("NotifyLeave");
        notifiedLeaveChannel = true;
    }

    @Override
    public String getUsername() {
        return username;
    }

    public String getTargetUsername() {
        return requestedPrivateChatUsername;
    }

    public void resetPrivateChatData() {
        requestedPrivateChat = false;
        requestedPrivateChatUsername = "";
    }

    public boolean recievedNotifyLeave() {
        return notifiedLeaveChannel;
    }

    public void resetNotifyLeave() {
        notifiedLeaveChannel = false;
    }
}
