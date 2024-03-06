package server;

import client.IRCClientInterface;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

public class Channel {
    private String name;
    private ConcurrentHashMap<String, IRCClientInterface> clients;

    public Channel(String channelName) {
        name = channelName;
        clients = new ConcurrentHashMap<>();
    }

    public String getName() {
        return name;
    }

    public ConcurrentHashMap<String, IRCClientInterface> getClients() {
        return clients;
    }

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

    public void addClient(String username, IRCClientInterface clientInterface) {
        sendMessage("*server*", "User " + username + " joined the channel.");
        clients.put(username, clientInterface);
    }

    public IRCClientInterface removeClient(String username) {
        IRCClientInterface ret = clients.get(username);
        clients.remove(username);
        sendMessage("*server*", "User " + username + " left the channel.");
        return ret;
    }
}
