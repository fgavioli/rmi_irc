package server;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.PublicKey;
import java.util.ArrayList;

public interface IRCServerInterface extends Remote {

    /**
     * Procedure to connect to the server
     * @param username the username of the client
     * @param publicKey the public key of the client
     * @param signedFingerprint the signed username
     * @return 0 if the connection is successful, -1 otherwise
     */
    int connect(String username, PublicKey publicKey, byte[] signedFingerprint) throws RemoteException;

    /**
     * Returns a greeting to the server
     * @return the greeting
     */
    String getGreeting() throws RemoteException;

    /**
     * Returns the users in lobby
     * @return a list containing the usernames in lobby
     */
    ArrayList<String> getUsers() throws RemoteException;

    /**
     * Returns the channel names list
     * @return the channel names list
     */
    ArrayList<String> getChannelDescriptions() throws RemoteException;

    /**
     * Joins a channel
     * @param username the client that wants to join the channel
     * @param channelName the channel name
     * @param signedFingerprint the signed usename+channel string
     * @return 0 if the join was successful, -1 otherwise
     */
    int joinChannel(String username, String channelName, byte[] signedFingerprint) throws RemoteException;

    /**
     * Leaves the channel
     * @param username the client that wants to leave the channel
     * @param channelName the name of the channel to leave
     * @param signedFingerprint the signed username+channel string
     */
    void leaveChannel(String username, String channelName, byte[] signedFingerprint) throws RemoteException;

    /**
     * Join a private chat with another client
     * @param username the client that wants to join the chat
     * @param targetUsername the target client of the private chat
     * @param signedFingerprint the signed  string
     * @return 0 in case of success, -1 if the client was unreachable, -2 if the client refused the private chat, -3 if the signature verification failed
     */
    int joinPrivateChat(String username, String targetUsername, byte[] signedFingerprint) throws RemoteException;

    /**
     * Disconnects a client from the server
     * @param username the client to disconnect
     * @param signedFingerprint the signed username
     */
    void disconnect(String username, byte[] signedFingerprint) throws RemoteException;

    /**
     * Sends a message to a channel
     * @param username the username of the sender
     * @param channel the channel to send the message to
     * @param message the message to be sent
     * @param signedFingerprint the signature of the message
     */
    void sendMessage(String username, String channel, String message, byte[] signedFingerprint) throws RemoteException;
}