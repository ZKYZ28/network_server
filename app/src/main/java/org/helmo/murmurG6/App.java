package org.helmo.murmurG6;

import org.helmo.murmurG6.infrastructure.UserJsonStorage;
import org.helmo.murmurG6.controller.ServerController;
import java.io.IOException;

public class App {

    private static final int DEFAULT_PORT = 12345;
    private static final UserJsonStorage USER_JSON_STORAGE = new UserJsonStorage();

    private final

    public static void main(String[] args){
        try (ServerController server = new ServerController(DEFAULT_PORT, USER_JSON_STORAGE)) {
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}