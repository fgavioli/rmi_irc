package server;

import client.IRCClientInterface;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

public class Channel {
    private String name;
    private ConcurrentHashMap<String, IRCClientInterface> clients;

    /**
     * Channel constructor
     * @param channelName the channel name
     */
    public Channel(String channelName) {
        name = channelName;
        clients = new ConcurrentHashMap<>();
    }

    /**
     * Returns the name of the channel
     * @return the channel name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the Hash Map containing the clients
     * @return the clients
     */
    public ConcurrentHashMap<String, IRCClientInterface> getClients() {
        return clients;
    }

    /**
     * Broadcasts a message to all the clients in the channel except the sender
     * @param senderUsername the sender of the message
     * @param message the message to be sent
     */
    public void sendMessage(String senderUsername, String message) {
        Vector<String> usersRemaining = new Vector<>(clients.keySet());
        usersRemaining.remove(senderUsername);

        while (!usersRemaining.isEmpty()) {
            ArrayList<String> toRemove = new ArrayList<>();
            try {
                for (String username : usersRemaining) {
                    toRemove.add(username);
                    clients.get(username).sendMessage(senderUsername, message);
                }
            } catch (RemoteException ignored) {}
            usersRemaining.removeAll(toRemove);
        }
    }

    /**
     * Adds a client to the channel
     * @param username the username of the client to be added
     * @param clientInterface the remote client object
     */
    public void addClient(String username, IRCClientInterface clientInterface) {
        sendMessage("*server*", "User " + username + " joined the channel.");
        clients.put(username, clientInterface);
    }

    /**
     * Removes a client from the channel
     * @param username the username of the client to be removed
     * @return the client interface of the removed client
     */
    public IRCClientInterface removeClient(String username) {
        IRCClientInterface ret = clients.get(username);
        clients.remove(username);
        sendMessage("*server*", "User " + username + " left the channel.");
        return ret;
    }
}
