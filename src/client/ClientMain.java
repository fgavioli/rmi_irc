package client;

import java.net.MalformedURLException;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

public class ClientMain {
    public static void main(String[] args) {
        Scanner stdin = new Scanner(System.in);
        System.out.println("*** RMI-IRC Client ***\n");
        System.setProperty("java.rmi.server.hostname","192.168.1.37");
        System.out.print("Type the server name you want to connect to: ");
        String serverName = stdin.nextLine();
        System.out.print("Username: ");
        String username = stdin.nextLine();
        // Start client session
        ClientSession cs = null;
        try {
            cs = new ClientSession(username);
        } catch (AlreadyBoundException e) {
            System.err.println("Username " + username + " is already registered.");
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            System.err.print("Unsupported ciphers.");
        } catch (MalformedURLException e) {
            System.err.println("Username " + username + " is invalid.");
        } catch (RemoteException e) {
            System.err.println("Server " + serverName + " does not exist.");
        }
        if (cs != null) {
            int ret = -1;
            try {
                ret = cs.start(serverName);
            } catch (RemoteException e) {
                System.err.println("Connection lost.");
            }
            System.exit(ret);
        } else {
            System.exit(-1);
        }
    }
}

