package server;

import java.util.ArrayList;

public class Channel {
    private String name;
    private ArrayList<String> clients;

    public Channel(String channelName) {
        name = channelName;
        clients = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public ArrayList<String> getClients() {
        return clients;
    }

    public void addClient(String s) {
        clients.add(s);
    }

    public void removeClient(String s) {
        clients.remove(s);
    }
}
