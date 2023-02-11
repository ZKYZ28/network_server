package org.helmo.murmurG6;

import org.helmo.murmurG6.infrastructure.storage.json.UserJsonStorage;
import org.helmo.murmurG6.models.User;
import org.helmo.murmurG6.models.UserCollection;
import org.helmo.murmurG6.repository.exceptions.SaveUserCollectionException;
import org.helmo.murmurG6.system.ServerController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class App {

    private static final int DEFAULT_PORT = 12345;
    private static final UserJsonStorage USER_JSON_STORAGE = new UserJsonStorage();


    public static void main(String[] args) throws IOException {
        try (ServerController server = new ServerController(DEFAULT_PORT, USER_JSON_STORAGE)) {
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
