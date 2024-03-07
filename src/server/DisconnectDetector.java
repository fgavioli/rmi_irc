package server;

import client.IRCClientInterface;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class DisconnectDetector implements Runnable {
    private IRCServer server;

    public DisconnectDetector(IRCServer server) {
        this.server = server;
    }

    private void checkClients(ConcurrentHashMap<String, IRCClientInterface> clients) {
        ArrayList<String> clientsToCheck = new ArrayList<>(clients.keySet());
        while (!clientsToCheck.isEmpty()) {
            ArrayList<String> checkedClients = new ArrayList<>();
            String lastUser = "";
            try {
                for (String user : clientsToCheck) {
                    lastUser = user;
                    checkedClients.add(user);
                    clients.get(user).getUsername();
                }
            } catch (RemoteException e) {
                // remove client from lobby
                server.removeClient(lastUser);
            }
            // remove all checked clients from the list and restart the check with the remaining ones
            clientsToCheck.removeAll(checkedClients);
        }
    }

    @Override
    public void run() {

        // Check disconnected users in lobby
        ConcurrentHashMap<String, IRCClientInterface> clients = new ConcurrentHashMap<>(server.getClientsInLobby());
        checkClients(clients);
        clients.clear();

        // Check disconnected users in channels
        ArrayList<Channel> channels = new ArrayList<>(server.getChannels());
        for (Channel c : channels)
            clients.putAll(c.getClients());
        checkClients(clients);
        clients.clear();

        // Check private chats
//        ArrayList<Channel> privateChats = new ArrayList<>(server.getPrivateChats());


// CHECK PRIVATE CHATS AND DESTROY IF ONE OF THE CLIENTS IS OFFLINE
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