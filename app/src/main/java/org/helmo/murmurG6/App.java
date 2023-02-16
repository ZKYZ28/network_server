package org.helmo.murmurG6;

import org.helmo.murmurG6.infrastructure.UserJsonStorage;
import org.helmo.murmurG6.controller.ServerController;
import java.io.IOException;

public class App {

    static {
        System.setProperty("javax.net.ssl.keyStore", "app/src/main/resources/star.godswila.guru.p12");
        System.setProperty("javax.net.ssl.keyStorePassword", "labo2023");
    }

    private static final int DEFAULT_PORT = 23106;
    private static final UserJsonStorage USER_JSON_STORAGE = new UserJsonStorage();

    public static void main(String[] args){
        try (ServerController server = new ServerController(DEFAULT_PORT, USER_JSON_STORAGE)) {
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}