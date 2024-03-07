package client;

import server.IRCServerInterface;

import java.net.MalformedURLException;
import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.Scanner;

public class ClientSession {
    private IRCClient client;
    private SignatureManager sm;
    private IRCServerInterface server;

    public ClientSession(String username) throws NoSuchAlgorithmException, InvalidKeyException, MalformedURLException, AlreadyBoundException, RemoteException {
        // Bind local client object
        client = new IRCClient(username);
        Naming.bind(username, client);
        sm = new SignatureManager();
    }

    public void start(String serverName) throws SignatureException, NoSuchAlgorithmException, InvalidKeyException, RemoteException, MalformedURLException, NotBoundException {
        // Lookup remote server object
        server = (IRCServerInterface) Naming.lookup(serverName);
        int seed = server.connect(client.getUsername(), sm.getPublicKey(), sm.sign(client.getUsername().getBytes()));
        if (seed == 0) {
            System.err.println("Connection error.");
            return;
        }
        sm.setSeed(seed);
        System.out.println(server.getGreeting());
        while(true)
            lobbyMenuLoop();
    }


    private void printMenu() {
        System.out.println("\nChoose a service:");
        System.out.println("\t1. List channels");
        System.out.println("\t2. List users");
        System.out.println("\t3. Join channel");
        System.out.println("\t4. Open private chat");
    }

    private void lobbyMenuLoop() throws RemoteException, SignatureException, NoSuchAlgorithmException, InvalidKeyException {
        Scanner stdin = new Scanner(System.in);
        printMenu();
        String option = stdin.nextLine();
        switch (option) {
            case "1":
                ArrayList<String> channels = server.getChannelNames();
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
                String channelName = stdin.nextLine();
                while(channelName.startsWith("#"))
                    channelName = channelName.substring(1, channelName.length()-1);
                if (!channelName.equals("q")) {
                    byte[] signedFingerprint = sm.signWithNonce((client.getUsername() + channelName).getBytes());
                    int ret = server.joinChannel(client.getUsername(), channelName, signedFingerprint);
                    if (ret == 0)
                        chatLoop(channelName);
                    else {
                        System.err.println("Unable to join channel " + channelName + ".");
                    }
                } else {
                    server.disconnect(client.getUsername(), sm.sign(client.getUsername().getBytes()));
                    System.exit(0);
                }
                break;
            case "4":
                System.out.print("Type the user you want to start a private chat with or q to exit [q]:");
                String targetUsername = stdin.nextLine();
                if (!targetUsername.equals("q")) {
                    byte[] signedFingerprint = sm.signWithNonce((client.getUsername() + targetUsername).getBytes());
                    int ret = server.joinPrivateChat(client.getUsername(), targetUsername, signedFingerprint);
                    switch (ret) {
                        case 0:
                            chatLoop("private");
                            break;
                        case -1:
                            System.out.println("Unable to start private chat with " + targetUsername);
                            break;
                        case -2:
                            break;
                        case -3:

                    }
                }
                break;
            default:
                System.out.println("Unrecognized option, please retry.");
        }

    }

    private void chatLoop(String channel) throws SignatureException, NoSuchAlgorithmException, InvalidKeyException, RemoteException {
        System.out.println("Joined channel " + channel + ". Write a message, press enter to send. Send \":q\" to quit.");
        // read stdin
        Scanner scanner = new Scanner(System.in);
        String msg = scanner.nextLine();
        while(!msg.equals(":q")) {
            server.sendMessage(client.getUsername(), channel, msg, sm.signWithNonce(msg.getBytes()));
            msg = scanner.nextLine();
        }
        server.leaveChannel(client.getUsername(), channel, sm.signWithNonce((client.getUsername() + channel).getBytes()));
    }
}