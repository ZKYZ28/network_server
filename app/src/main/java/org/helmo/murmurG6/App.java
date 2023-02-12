package org.helmo.murmurG6;

import org.helmo.murmurG6.infrastructure.UserJsonStorage;
import org.helmo.murmurG6.system.ServerController;
import java.io.IOException;

public class App {

    /*
     * Chargement des propriétés SSL dans un bloc static afin d'avoir ces propriétés
     * pour toute l'instance de l'application
     */
    static {
        System.setProperty("javax.net.ssl.keyStore", "./org/helmo/murmurG6/ssl/star.godswila.guru.p12");
        System.setProperty("javax.net.ssl.keyStorePassword", "labo2023");
    }

    private static final int DEFAULT_PORT = 12345;
    private static final UserJsonStorage USER_JSON_STORAGE = new UserJsonStorage();

    public static void main(String[] args){
        try (ServerController server = new ServerController(DEFAULT_PORT, USER_JSON_STORAGE)) {
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

    //python main.py 192.168.1.63 12345 admin admin