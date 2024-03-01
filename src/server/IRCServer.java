package server;

import client.IRCClientInterface;

import javax.crypto.NoSuchPaddingException;
import java.net.MalformedURLException;
import java.nio.ByteBuffer;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.*;
import java.util.ArrayList;
import java.util.HashMap;

public class IRCServer extends UnicastRemoteObject implements IRCServerInterface {
    private String name;
    private ArrayList<Channel> channels;
    private ArrayList<IRCClientInterface> clients;
    private HashMap<String, Signature> clientKeys;

    public IRCServer(String serverName) throws RemoteException, NoSuchPaddingException, NoSuchAlgorithmException {
        super();
        this.name = serverName;
        channels = new ArrayList<>();
        clients = new ArrayList<>();
        clientKeys = new HashMap<>();
    }

    @Override
    public int connect(String username, PublicKey publicKey, byte[] signedFingerprint) throws RemoteException {
        System.out.println("Recieved connection request from username: " + username + ".");
        if (username == null || username.isEmpty())
            return -1;
        for (IRCClientInterface client : clients) {
            if (client.getUsername().equals(username))
                return -1;
        }
        try {
            // Init authentication objects
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initVerify(publicKey);
            // Check that the cipher works
            signature.update(username.getBytes());
            if(!signature.verify(signedFingerprint))
                System.err.println("[INFO] invalid signature detected, unable to connect client " + username);
            clientKeys.put(username, signature);

            // Add client into lobby
            IRCClientInterface client = (IRCClientInterface) Naming.lookup(username);
            clients.add(client);
        } catch (NotBoundException | MalformedURLException | RemoteException | NoSuchAlgorithmException |
                 InvalidKeyException | SignatureException e) {
            e.printStackTrace();
            return -1;
        }
        return 0;
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
        ArrayList<String> ret = new ArrayList<>();
        for (IRCClientInterface c : clients)
            ret.add(c.getUsername());
        return ret;
    }

    @Override
    public ArrayList<String> getChannels() throws RemoteException {
        ArrayList<String> ret = new ArrayList<>();
        for (Channel c : channels)
            ret.add(c.getName());
        return ret;
    }

    @Override
    public int joinChannel(String username, String channelName, byte[] nonce, byte[] SignedFingerprint) {
        System.out.println("Not implemented yet");
//        for (server.Channel c : channels)
//            if (c.getName().equals(channelName))
//                c.addClient(username);
        return 0;
    }

    @Override
    public int joinPrivateChat(String username, String targetUsername, byte[] nonce, byte[] signedFingerprint) throws SignatureException {
        // Verify signature
        if (verifySignature(username, (username + targetUsername).getBytes(), nonce, signedFingerprint)) {
            // joinProcedure
            // add client to channel
            // remove client from lobby
            ;
        } else {
            // disconnectProcedure
            // remove client from list like it disconnected willingly
            // return error code to client
            return -1;
        }
        return 0;
    }

    private boolean verifySignature(String username, byte[] message, byte[] nonce, byte[] signedFingerprint) throws SignatureException {
        byte[] msg = new byte[message.length + nonce.length];
        ByteBuffer bb = ByteBuffer.wrap(msg);
        bb.put(message);
        bb.put(nonce);
        clientKeys.get(username).update(bb.array());
        return clientKeys.get(username).verify(signedFingerprint);
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

}
