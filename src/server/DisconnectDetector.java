package server;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Objects;

public class DisconnectDetector implements Runnable {
    private IRCServer server;

    public DisconnectDetector(IRCServer server) {
        this.server = server;
    }
    @Override
    public void run() {
        // TODO: RESTRUCTURE THIS FOLLOWING SENDMESSAGE
        ArrayList<String> allClients;
//        System.out.print("Lobby: ");
//        for (String user : server.getClientsInLobby().keySet()) {
//            try { // call getUsername to check if the client is still alive
//                if (!server.getClientsInLobby().get(user).getUsername().equals(user))
//                    throw new RemoteException();
//            } catch (RemoteException e) {
//                // client disconnected
//                server.removeClient(user);
//            }
//            System.out.print(user + " ");
//        }
//        for (Channel ch : server.getChannels()) {
////            System.out.print("\nChannel " + c + ": ");
//            for (String c : ch.getClients().keySet()) {
//                try { // call getUsername to check if the client is still alive
//                    if (ch.getClients().get(c).getUsername().equals(c))
//                }
//            } catch (RemoteException e) {
//                // client disconnected
//                server.removeClient(user);
//            }
//        }
    }
}