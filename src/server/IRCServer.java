package server;

import client.IRCClientInterface;

import java.io.IOException;
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
    private Vector<Channel> privateChats = new Vector<>();


    /**
     * IRCServer constructor
     * @param serverName the name of the server
     */
    public IRCServer(String serverName) throws RemoteException {
        super();
        this.name = serverName;
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(new DisconnectDetector(this), 20, 20, TimeUnit.SECONDS);
    }

    /**
     * Procedure to connect to the server
     * @param username the username of the client
     * @param publicKey the public key of the client
     * @param signedFingerprint the signed username
     * @return 0 if the connection is successful, -1 otherwise
     */
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

    /**
     * Disconnects a client from the server
     * @param username the client to disconnect
     * @param signedFingerprint the signed username
     */
    public void disconnect(String username, byte[] signedFingerprint) throws RemoteException {
        if (signatureVerifier.verifySignature(username, username.getBytes(), signedFingerprint)) {
            removeClient(username);
        }
    }

    /**
     * Sends a message to a channel
     * @param username the username of the sender
     * @param channel the channel to send the message to
     * @param message the message to be sent
     * @param signedFingerprint the signature of the message
     */
    @Override
    public void sendMessage(String username, String channel, String message, byte[] signedFingerprint) throws RemoteException {
        System.out.println("[INFO] " + username + " sent message \"" + message + "\"" + " to channel \"" + channel + "\".");
        if (signatureVerifier.verifySignature(username, message.getBytes(), signedFingerprint)) {
            if (channel.startsWith("private_")) {
                for (Channel c : privateChats)
                    if (c.getName().equals(channel))
                        c.sendMessage(username, message);
            } else {
                for (Channel c : channels)
                    if (c.getName().equals(channel))
                        c.sendMessage(username, message);
            }
        } else {
            System.err.println("SIGNATURE VERIFICATION FAILED DURING SENDMESSAGE");
        }
    }

    /**
     * Removes a client from the server
     * @param username the username of the client to be removed
     */
    public void removeClient(String username)  {

        // remove client in lobby
        signatureVerifier.removeSignature(username);
        clientsInLobby.remove(username);

        // Remove client from channels
        for (Channel c : channels)
            c.removeClient(username);

        // Remove client from private chats
        Channel toRemove = null;
        for (Channel c : privateChats)
            if (c.getClients().containsKey(username)) {
                for (String u : c.getClients().keySet())
                    if (u.equals(username)) {
                        try {
                            IRCClientInterface iface = c.getClients().get(u);
                            iface.notifyLeave();
                            clientsInLobby.put(u, iface);
                        } catch (RemoteException e) {
                            c.removeClient(u);
                        }
                    } else {
                        c.removeClient(username);
                    }
                toRemove = c;
                break;
            }
        if (toRemove != null)
            privateChats.remove(toRemove);
    }

    /**
     * Returns a greeting to the server
     * @return the greeting
     */
    public String getGreeting() {
        StringBuilder greeting = new StringBuilder();
        greeting.append("Welcome to the ").append(this.name).append(" IRC Server!\n");
        greeting.append("Available channels: \n");
        for (Channel c : channels)
            greeting.append("    #").append(c.getName()).append("\t").append(c.getClients().keySet().size()).append(" users\n");
        return greeting.toString();
    }

    /**
     * Returns the users in lobby
     * @return a list containing the usernames in lobby
     */
    @Override
    public ArrayList<String> getUsers() throws RemoteException {
        return new ArrayList<>(clientsInLobby.keySet());
    }

    /**
     * Returns the channel names list
     * @return the channel names list
     */
    @Override
    public ArrayList<String> getChannelDescriptions() throws RemoteException {
        ArrayList<String> ret = new ArrayList<>();
        for (Channel c : channels)
            ret.add(c.getName() + " \t" + c.getClients().size() + " users");
        return ret;
    }

    /**
     * Returns the channels
     * @return the channels
     */
    public Vector<Channel> getChannels() {
        return channels;
    }

    /**
     * Returns the private chats
     * @return the private chats
     */
    public Vector<Channel> getPrivateChats() {
        return privateChats;
    }

    /**
     * Joins a channel
     * @param username the client that wants to join the channel
     * @param channelName the channel name
     * @param signedFingerprint the signed usename+channel string
     * @return 0 if the join was successful, -1 otherwise
     */
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

    /**
     * Leaves the channel
     * @param username the client that wants to leave the channel
     * @param channelName the name of the channel to leave
     * @param signedFingerprint the signed username+channel string
     */
    public void leaveChannel(String username, String channelName, byte[] signedFingerprint) {
        System.out.println("[INFO] Received leaveChannel(" + channelName + ") request from " + username + ".");
        if (signatureVerifier.verifySignature(username, (username+channelName).getBytes(), signedFingerprint)) {
            if (channelName.startsWith("private_")) {
                for (Channel c : privateChats)
                    if (c.getName().equals(channelName)) {
                        HashMap<String, IRCClientInterface> clientsInChannel = new HashMap<>(c.getClients());
                        for (String u : clientsInChannel.keySet()) {
                            try {
                                clientsInChannel.get(u).notifyLeave();
                            } catch (RemoteException ignored) {}
                            c.removeClient(u);
                        }
                        clientsInLobby.putAll(clientsInChannel);
                        privateChats.remove(c);
                        return;
                    }
            } else {
                for (Channel c : channels)
                    if (c.getName().equals(channelName)) {
                        IRCClientInterface client = c.removeClient(username);
                        clientsInLobby.put(username, client);
                    }
            }
        }
    }

    /**
     * Join a private chat with another client
     * @param username the client that wants to join the chat
     * @param targetUsername the target client of the private chat
     * @param signedFingerprint the signed  string
     * @return 0 in case of success, -1 if the client was unreachable, -2 if the client refused the private chat, -3 if the signature verification failed
     */
    @Override
    public int joinPrivateChat(String username, String targetUsername, byte[] signedFingerprint) {
        System.out.println("[INFO] Received joinPrivateChat(" + targetUsername + ") request from " + username + ".");
        // Verify signature
        if (signatureVerifier.verifySignature(username, (username + targetUsername).getBytes(), signedFingerprint)) {
            if (username.equals(targetUsername))
                return -1;
            if (!clientsInLobby.containsKey(targetUsername))
                return -1; // unable to connect to client
            try {
                if (clientsInLobby.get(targetUsername).requestPrivateChat(username)) {
                    // success
                    Channel c = new Channel("private_" + username);
                    c.addClient(username, clientsInLobby.get(username));
                    c.addClient(targetUsername, clientsInLobby.get(targetUsername));
                    clientsInLobby.remove(username);
                    clientsInLobby.remove(targetUsername);
                    privateChats.add(c);
                    return 0; // success
                } else {
                    return -2; // client refused private chat
                }
            } catch (IOException e) {
                removeClient(targetUsername);
                return -1; // unable to connect to client
            }
        } else {
            return -3; // signature verification failed
        }
    }

    /**
     * Add a channel to the server
     * @param channelName the name of the channel to add
     * @return 0 in case of success, -1 if the channel already exists
     */
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

    /**
     * Returns the list of the clients in lobby
     * @return the list of the clients in lobby
     */
    public ConcurrentHashMap<String, IRCClientInterface> getClientsInLobby() {
        return clientsInLobby;
    }
}
