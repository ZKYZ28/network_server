package org.helmo.murmurG6;

import org.helmo.murmurG6.infrastructure.OffLineMessagesJsonStorage;
import org.helmo.murmurG6.infrastructure.TrendJsonStorage;
import org.helmo.murmurG6.infrastructure.UserJsonStorage;
import org.helmo.murmurG6.controller.ServerController;
import org.helmo.murmurG6.repository.OffLineMessageRepository;
import org.helmo.murmurG6.repository.TrendRepository;
import org.helmo.murmurG6.repository.UserRepository;

import java.io.IOException;

public class App {

    static {
        System.setProperty("javax.net.ssl.keyStore", "app/src/main/resources/star.godswila.guru.p12");
        System.setProperty("javax.net.ssl.keyStorePassword", "labo2023");
    }

    private static final UserRepository USER_REPOSITORY = new UserJsonStorage();
    private static final TrendRepository TREND_REPOSITORY = new TrendJsonStorage();
    private static final OffLineMessageRepository OFFLINE_MESSAGES_REPOSITORY = new OffLineMessagesJsonStorage();

    public static void main(String[] args) {
        try (ServerController server = ServerController.getInstance()) {
            server.init(USER_REPOSITORY, TREND_REPOSITORY, OFFLINE_MESSAGES_REPOSITORY);
            server.start();
        } catch (IOException e) {
            System.out.println("Impossible de lancer le serveur.");
        }
    }
}