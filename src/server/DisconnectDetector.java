package server;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Objects;

public class DisconnectDetector implements Runnable {
    private IRCServer server;

    public DisconnectDetector(IRCServer server) {
        this.server = server;
    }
    @Override
    public void run() {
        for (String user : server.getClientsInLobby().keySet()) {
            try { // call getUsername to check if the client is still alive
                if (!server.getClientsInLobby().get(user).getUsername().equals(user))
                    throw new RemoteException();
            } catch (RemoteException e) {
                // client disconnected
                server.removeClient(user);
            }
        }
    }
}