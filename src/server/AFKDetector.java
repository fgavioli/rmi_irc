package server;

import java.rmi.RemoteException;

public class AFKDetector implements Runnable {
    private IRCServer server;

    public AFKDetector(IRCServer server) {
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
