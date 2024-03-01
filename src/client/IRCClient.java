package client;

import java.io.Serializable;
import java.security.*;

public class IRCClient implements IRCClientInterface, Serializable {
    private final String username;

    public IRCClient(String username) throws NoSuchAlgorithmException, InvalidKeyException {
        this.username = username;

    }

    @Override
    public String getUsername() {
        return username;
    }
}
