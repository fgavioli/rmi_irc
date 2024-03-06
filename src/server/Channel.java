package server;

import client.IRCClientInterface;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
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
        for (String username : clients.keySet())
            ; //clients.get(username).sendMessage();
    }

    public void addClient(String username, IRCClientInterface clientInterface) {
        clients.put(username, clientInterface);
    }

    public void removeClient(String username) {
        clients.remove(username);
    }
}
