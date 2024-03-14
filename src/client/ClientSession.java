package client;

import server.IRCServerInterface;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

public class ClientSession {
    private IRCClient client;
    private SignatureManager sm;
    private IRCServerInterface server;
    private ReentrantLock stdinLock = new ReentrantLock();
    private BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));

    public ClientSession(String username) throws NoSuchAlgorithmException, InvalidKeyException, MalformedURLException, AlreadyBoundException, RemoteException {
        // Bind local client object
        client = new IRCClient(username, this);
        Naming.bind(username, client);
        sm = new SignatureManager();
    }

    public int start(String serverName) throws RemoteException {
        // Lookup remote server object
        try {
            server = (IRCServerInterface) Naming.lookup(serverName);
        } catch (MalformedURLException | NotBoundException | RemoteException e) {
            System.err.println("Unable to connect to server " + serverName + ".");
            return -1;
        }
        int seed = 0;
        try {
            seed = server.connect(client.getUsername(), sm.getPublicKey(), sm.sign(client.getUsername().getBytes()));
        } catch (SignatureException | NoSuchAlgorithmException | InvalidKeyException e) {
            System.out.println("Unsupported ciphers.");
            return -1;
        }
        if (seed == 0) {
            System.err.println("Seed initialization error.");
            return -1;
        }
        sm.setSeed(seed);
        System.out.println(server.getGreeting());
        try {
            while (lobbyMenuLoop() == 1)
                ; // continue looping
        } catch (RemoteException e) {
            System.err.println("Server connection lost");
            return -1;
        } catch (IOException e) {
            System.err.println("Error while reading standard input.");
            return -1;
        } catch (SignatureException | NoSuchAlgorithmException | InvalidKeyException e) {
            System.out.println("Unsupported ciphers.");
            return -1;
        }
        return 0;
    }

    private void printMenu() {
        System.out.println("\nChoose a service:");
        System.out.println("\t1. List channels");
        System.out.println("\t2. List users");
        System.out.println("\t3. Join channel");
        System.out.println("\t4. Open private chat");
        System.out.println("\t5. Quit");
    }

    private int lobbyMenuLoop() throws IOException, SignatureException, NoSuchAlgorithmException, InvalidKeyException {
        printMenu();
        String option = null;
        while (option == null) {
            // wait until stdin has something or we join a private chat
            while (!stdin.ready()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ignored) {}
                if (client.hasRequestedPrivateChat()) {
                    String target = client.getTargetUsername();
                    client.resetPrivateChatData();
                    chatLoop("private_" + target);
                    client.resetNotifyLeave();
                    printMenu();
                }
            }

            lockStdin();
            try {
                if (stdin.ready()) {
                    option = stdin.readLine();
                }
            } catch (IOException ignored) {}
            unlockStdin();
        }
        switch (option) {
            case "1":
                ArrayList<String> channels = server.getChannelDescriptions();
                System.out.println("Available channels:");
                for (String c : channels)
                    System.out.println("\t#" + c);
                break;
            case "2":
                ArrayList<String> users = server.getUsers();
                System.out.println("Users in lobby:");
                for (String u : users)
                    System.out.println("\t" + u);
                break;
            case "3":
                System.out.print("Type the channel you want to join or q to exit [q]:");
                String channelName = stdin.readLine();
                while(channelName.startsWith("#"))
                    channelName = channelName.substring(1, channelName.length()-1);
                if (!channelName.equals("q")) {
                    byte[] signedFingerprint = sm.signWithNonce((client.getUsername() + channelName).getBytes());
                    int ret = server.joinChannel(client.getUsername(), channelName, signedFingerprint);
                    client.resetNotifyLeave();
                    if (ret == 0)
                        chatLoop(channelName);
                    else {
                        System.err.println("Unable to join channel " + channelName + ".");
                    }
                }
                break;
            case "4":
                System.out.print("Type the user you want to start a private chat with or q to exit [q]: ");
                String targetUsername = stdin.readLine();
                System.out.println("Waiting for " + targetUsername + "...");
                if (!targetUsername.equals("q")) {
                    byte[] signedFingerprint = sm.signWithNonce((client.getUsername() + targetUsername).getBytes());
                    int ret = server.joinPrivateChat(client.getUsername(), targetUsername, signedFingerprint);
                    client.resetNotifyLeave();
                    switch (ret) {
                        case 0:
                            chatLoop("private_" + client.getUsername());
                            break;
                        case -1:
                            System.out.println("Unable to start private chat with " + targetUsername);
                            break;
                        case -2:
                            System.out.println(targetUsername + " refused your invite.");
                            break;
                        case -3:
                            System.out.println("Signature verification failed.");
                            break;
                    }
                }
                break;
            case "5":
                server.disconnect(client.getUsername(), sm.sign(client.getUsername().getBytes()));
                return 0;
            default:
                System.out.println("Unrecognized option, please retry.");
        }
        return 1;
    }

    private void chatLoop(String channel) throws SignatureException, NoSuchAlgorithmException, InvalidKeyException, IOException {
        System.out.println("Joined channel " + channel + ". Write a message, press enter to send. Send \":q\" to quit.");
        // read stdin
        String msg = "";
        while (!msg.equals(":q")) {
            if (!msg.isEmpty())
                server.sendMessage(client.getUsername(), channel, msg, sm.signWithNonce(msg.getBytes()));
            while (!stdin.ready()) {
                try {
                    Thread.sleep(100);
                    if (client.recievedNotifyLeave()) {
                        client.resetNotifyLeave();
                        System.out.println("Channel closed. Leaving.");
//                        server.leaveChannel(client.getUsername(), channel, sm.signWithNonce((client.getUsername() + channel).getBytes()));
                        return;
                    }
                } catch (InterruptedException ignored) {}
            }
            msg = stdin.readLine();
        }
        client.resetNotifyLeave();
        server.leaveChannel(client.getUsername(), channel, sm.signWithNonce((client.getUsername() + channel).getBytes()));
    }

    public void lockStdin() {
        stdinLock.lock();
    }

    public void unlockStdin() {
        stdinLock.unlock();
    }

    public BufferedReader getScanner() {
        return this.stdin;
    }
}