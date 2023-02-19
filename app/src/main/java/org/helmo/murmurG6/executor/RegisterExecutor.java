package org.helmo.murmurG6.executor;

import org.helmo.murmurG6.controller.ClientRunnable;
import org.helmo.murmurG6.controller.Protocol;
import org.helmo.murmurG6.controller.ServerController;
import org.helmo.murmurG6.models.BCrypt;
import org.helmo.murmurG6.models.User;
import org.helmo.murmurG6.models.UserCredentials;
import org.helmo.murmurG6.models.exceptions.UserAlreadyRegisteredException;
import org.helmo.murmurG6.repository.exceptions.UnableToSaveTrendLibraryException;
import org.helmo.murmurG6.repository.exceptions.UnableToSaveUserLibraryException;

import java.util.HashSet;
import java.util.regex.Matcher;

public class RegisterExecutor {

    private static final ServerController server = ServerController.getInstance();


    protected static void register(ClientRunnable client, Matcher params){
        User user = new User(
                new UserCredentials(params.group("username"), server.getServerConfig().getServerName()),
                BCrypt.of(params.group("bcrypt")),
                new HashSet<>(),
                new HashSet<>());

        client.sendMessage(executeRegister(user, client));
    }

    private static String executeRegister(User user, ClientRunnable client) {
        try {
            server.getUserLibrary().register(user);
            client.setUser(user);
            server.save();
            return Protocol.build_OK();
        } catch (UnableToSaveUserLibraryException | UnableToSaveTrendLibraryException | UserAlreadyRegisteredException e) {
            return Protocol.build_ERROR();
        }
    }
}
