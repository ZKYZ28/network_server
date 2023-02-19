package org.helmo.murmurG6.executor;

import org.helmo.murmurG6.controller.ClientRunnable;
import org.helmo.murmurG6.models.Protocol;
import org.helmo.murmurG6.controller.ServerController;
import org.helmo.murmurG6.models.User;

import java.util.regex.Matcher;

public class ConnectExecutor {

    private static final ServerController server = ServerController.getInstance();

    protected static void connect(ClientRunnable client, Matcher params){
        client.sendMessage(controlConnect(client, params.group("username")));
    }

    private static String controlConnect(ClientRunnable client, String login) {
        if (server.getUserLibrary().isRegistered(login)) {
            User user = server.getUserLibrary().getUser(login);
            client.setUser(user);
            return Protocol.build_PARAM(user.getBcryptRound(), user.getBcryptSalt());
        } else {
            return Protocol.build_ERROR();
        }
    }
}
