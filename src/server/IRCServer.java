package server;

import client.IRCClientInterface;

import java.net.MalformedURLException;
import java.nio.ByteBuffer;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class IRCServer extends UnicastRemoteObject implements IRCServerInterface {
    private final String name;
    private ArrayList<Channel> channels = new ArrayList<>();
    private HashMap<String, IRCClientInterface> clientsInLobby = new HashMap<>();
    private SignatureVerifier signatureVerifier = new SignatureVerifier();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public IRCServer(String serverName) throws RemoteException {
        super();
        this.name = serverName;
    }

    @Override
    public int connect(String username, PublicKey publicKey, byte[] signedFingerprint) throws RemoteException {
        System.out.println("Received connection request from username: " + username + ".");

        if (username == null || username.isEmpty())
            return -1;

        // check if the same username is already connected
        for (String u : clientsInLobby.keySet()) {
            if (u.equals(username))
                return -1;
        }

        try {
            signatureVerifier.addSignature(username, publicKey);
            if(!signatureVerifier.verifySignature(username, username.getBytes(), null, signedFingerprint)) {
                signatureVerifier.removeSignature(username);
                System.err.println("[INFO] invalid signature detected, unable to connect client " + username);
                return -1;
            }

            // Add client to lobby
            IRCClientInterface client = (IRCClientInterface) Naming.lookup(username);
            clientsInLobby.put(username, client);
        } catch (NotBoundException | MalformedURLException | RemoteException | NoSuchAlgorithmException |
                 InvalidKeyException e) {
            signatureVerifier.removeSignature(username);
            e.printStackTrace();
            return -1;
        }
        return 0;
    }

    public int disconnect(String username, byte[] nonce, byte[] signedFingerprint) throws RemoteException {
        if (signatureVerifier.verifySignature(username, username.getBytes(), nonce, signedFingerprint)) {
            removeClient(username);
        }
        return 0;
    }

    public void removeClient(String username)  {
        // Remove client from channels
        for (Channel c : channels)
            c.removeClient(username);
        signatureVerifier.removeSignature(username);
        clientsInLobby.remove(username);
    }

    public String getGreeting() {
        StringBuilder greeting = new StringBuilder();
        greeting.append("Welcome to the ").append(this.name).append(" IRC Server!\n");
        greeting.append("Available channels: \n");
        for (Channel c : channels)
            greeting.append("    #").append(c.getName()).append("\n");
        return greeting.toString();
    }

    @Override
    public ArrayList<String> getUsers() throws RemoteException {
        return new ArrayList<>(clientsInLobby.keySet());
    }

    @Override
    public ArrayList<String> getChannels() throws RemoteException {
        ArrayList<String> ret = new ArrayList<>();
        for (Channel c : channels)
            ret.add(c.getName());
        return ret;
    }

    @Override
    public int joinChannel(String username, String channelName, byte[] nonce, byte[] signedFingerprint) {
        if (signatureVerifier.verifySignature(username, (username + channelName).getBytes(), nonce, signedFingerprint)) {
            for (server.Channel c : channels)
                if (c.getName().equals(channelName)) {
                    c.addClient(username, clientsInLobby.get(username));
                    clientsInLobby.remove(username);
                    break;
                }
            return 0;
        } else {
            removeClient(username);
            return -1;
        }
    }

    @Override
    public int joinPrivateChat(String username, String targetUsername, byte[] nonce, byte[] signedFingerprint) throws SignatureException {
        // Verify signature
        if (signatureVerifier.verifySignature(username, (username + targetUsername).getBytes(), nonce, signedFingerprint)) {
            // joinProcedure
            // add client to channel
            // remove client from lobby
            return 0;
        } else {
            // disconnectProcedure
            // remove client from list like it disconnected willingly
            // return error code to client
            return -1;
        }
    }

    public int addChannel(String channelName) {
        if (channelName.isEmpty()) {
            return -1;
        }
        // check for duplicates
        for (Channel c : channels)
            if (c.getName().equals(channelName))
                return -1;
        Channel c = new Channel(channelName, signatureVerifier);
        channels.add(c);
        return 0;
    }

    /**
     *
     * @return A list containing all clients inside the lobby and public channels
     */
    public HashMap<String, IRCClientInterface> getClientsInLobby() {
        HashMap<String, IRCClientInterface> clientList = new HashMap<>(clientsInLobby);
        for (Channel c : channels)
            clientList.putAll(c.getClients());
        return clientList;
    }
}
