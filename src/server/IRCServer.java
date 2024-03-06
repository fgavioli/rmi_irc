package server;

import client.IRCClientInterface;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class IRCServer extends UnicastRemoteObject implements IRCServerInterface {
    private final String name;
    private Vector<Channel> channels = new Vector<>();
    private ConcurrentHashMap<String, IRCClientInterface> clientsInLobby = new ConcurrentHashMap<>();
    private SignatureVerifier signatureVerifier = new SignatureVerifier();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);


    public IRCServer(String serverName) throws RemoteException {
        super();
        this.name = serverName;
        scheduler.schedule(new DisconnectDetector(this), 20, TimeUnit.SECONDS);
    }

    @Override
    public int connect(String username, PublicKey publicKey, byte[] signedFingerprint) throws RemoteException {
        System.out.println("[INFO] Received connection request from username: " + username + ".");

        if (username == null || username.isEmpty())
            return -1;

        // check if the same username is already connected
        if (clientsInLobby.containsKey(username))
            return -1;
        for (Channel c : channels)
            if (c.getClients().containsKey(username))
                return -1;

        try {
            int seed = signatureVerifier.addSignature(username, publicKey);
            if(!signatureVerifier.verifySignatureWithoutNonce(username, username.getBytes(), signedFingerprint)) {
                signatureVerifier.removeSignature(username);
                System.err.println("[INFO] Signature sanity check failed, unable to connect client" + username + ".");
                return -1;
            }

            // Add client to lobby
            IRCClientInterface client = (IRCClientInterface) Naming.lookup(username);
            clientsInLobby.put(username, client);
            return seed;
        } catch (NotBoundException | MalformedURLException | RemoteException | NoSuchAlgorithmException |
                 InvalidKeyException e) {
            signatureVerifier.removeSignature(username);
            e.printStackTrace();
            return 0;
        }
    }

    public int disconnect(String username, byte[] signedFingerprint) throws RemoteException {
        if (signatureVerifier.verifySignature(username, username.getBytes(), signedFingerprint)) {
            removeClient(username);
        }
        return 0;
    }

    @Override
    public void sendMessage(String username, String channel, String message, byte[] signedFingerprint) throws RemoteException {
        System.out.println("[INFO] " + username + " sent message \"" + message + "\"" + " to channel \"" + channel + "\".");
        if (signatureVerifier.verifySignature(username, message.getBytes(), signedFingerprint)) {
            for (Channel c : channels)
                if (c.getName().equals(channel))
                    c.sendMessage(username, message);
        } else {
            System.err.println("SIGNATURE VERIFICATION FAILED DURING SENDMESSAGE");
        }
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
            greeting.append("    #").append(c.getName()).append("\t").append(c.getClients().keySet().size()).append(" users\n");
        return greeting.toString();
    }

    @Override
    public ArrayList<String> getUsers() throws RemoteException {
        return new ArrayList<>(clientsInLobby.keySet());
    }

    @Override
    public ArrayList<String> getChannelNames() throws RemoteException {
        ArrayList<String> ret = new ArrayList<>();
        for (Channel c : channels)
            ret.add(c.getName());
        return ret;
    }

    public Vector<Channel> getChannels() {
        return channels;
    }

    @Override
    public int joinChannel(String username, String channelName, byte[] signedFingerprint) {
        System.out.println("[INFO] Received JoinChannel(" + channelName + ") request from " + username + ".");
        if (signatureVerifier.verifySignature(username, (username + channelName).getBytes(), signedFingerprint)) {
            for (Channel c : channels)
                if (c.getName().equals(channelName)) {
                    c.addClient(username, clientsInLobby.get(username));
                    clientsInLobby.remove(username);
                    return 0;
                }
            System.err.println("Channel " + channelName + " does not exist.");
        }
        return -1;
    }
    public int leaveChannel(String username, String channelName, byte[] signedFingerprint) {
        if (signatureVerifier.verifySignature(username, (username+channelName).getBytes(), signedFingerprint)) {
            for (Channel c : channels)
                if (c.getName().equals(channelName)) {
                    IRCClientInterface client = c.removeClient(username);
                    clientsInLobby.put(username, client);
                    return 0;
                }
        }
        return -1;
    }

    @Override
    public int joinPrivateChat(String username, String targetUsername, byte[] signedFingerprint) {
        // Verify signature
        if (signatureVerifier.verifySignature(username, (username + targetUsername).getBytes(), signedFingerprint)) {
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
        Channel c = new Channel(channelName);
        channels.add(c);
        return 0;
    }


    public HashMap<String, IRCClientInterface> getClientsInLobby() {
        HashMap<String, IRCClientInterface> clientList = new HashMap<>(clientsInLobby);
//        for (Channel c : channels)
//            clientList.putAll(c.getClients());
        return clientList;
    }
}
