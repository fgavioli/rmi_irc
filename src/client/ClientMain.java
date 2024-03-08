package client;

import server.IRCServerInterface;

import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
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
        try {
            // Start client session
            ClientSession cs = new ClientSession(username);
            cs.start(serverName);
            // TODO: move try/catch clauses into ClientSession
        } catch (RemoteException | MalformedURLException | NoSuchAlgorithmException | InvalidKeyException |
                 SignatureException e) {
            e.printStackTrace();
        } catch (NotBoundException e) {
            System.err.println("Server " + serverName + "does not exist.");
        } catch (AlreadyBoundException e) {
            System.err.println("Username " + username + " is already registered.");
        } catch (IOException e) {
            System.err.println("Error while reading stdin.");
        }
    }

}

