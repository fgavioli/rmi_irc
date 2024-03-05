package server;

import java.rmi.RemoteException;

public class DisconnectDetector implements Runnable {
    private IRCServer server;

    public DisconnectDetector(IRCServer server) {
        this.server = server;
    }
    @Override
    public void run() {
        for (String user : server.getClientsInLobby().keySet()) {
            try {
                server.getClientsInLobby().get(user).getUsername();
            } catch (RemoteException e) {
                // client disconnected
                server.removeClient(user);
            }
        }
    }
}