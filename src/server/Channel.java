package server;

import client.IRCClientInterface;

import java.util.ArrayList;
import java.util.HashMap;

public class Channel {
    private String name;
    private HashMap<String, IRCClientInterface> clients;

    public Channel(String channelName) {
        name = channelName;
        clients = new HashMap<>();
    }

    public String getName() {
        return name;
    }

    public HashMap<String, IRCClientInterface> getClients() {
        return clients;
    }

    public sendMessage(String senderUsername, String message) {
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
