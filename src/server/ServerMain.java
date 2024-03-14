package server;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class ServerMain {
    public static void main(String[] args) {
        System.out.println("\n\n***  IRC SERVER BOOTING  ***");
        String[] channel_names = {"general", "random"};
        // Create Server
        try {
            Registry r = LocateRegistry.createRegistry(1099);
            System.setProperty("java.rmi.server.hostname","127.0.0.1");
            System.setProperty("java.rmi.server.hostname","127.0.0.1");
            String serverName = "UNIMORE0";
            IRCServer server = new IRCServer(serverName);
            // Add some channels
            for (String name : channel_names)
                if (server.addChannel(name) != 0)
                    System.err.println("Couldn't add channel #" + name + ".");
            Naming.rebind(serverName, server);
            System.out.println("[INFO] Server" + serverName + " bound.");
        } catch (RemoteException | MalformedURLException e) {
            e.printStackTrace();
        }
    }
}