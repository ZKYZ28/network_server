package org.helmo.murmurG6;

import org.helmo.murmurG6.infrastructure.TrendJsonStorage;
import org.helmo.murmurG6.infrastructure.UserJsonStorage;
import org.helmo.murmurG6.controller.ServerController;
import org.helmo.murmurG6.repository.TrendRepository;
import org.helmo.murmurG6.repository.UserRepository;

import java.io.IOException;

public class App {

    static {
        System.setProperty("javax.net.ssl.keyStore", "app/src/main/resources/star.godswila.guru.p12");
        System.setProperty("javax.net.ssl.keyStorePassword", "labo2023");
    }

    public static final int DEFAULT_PORT = 20200;
    private static final UserRepository USER_REPOSITORY = new UserJsonStorage();
    private static final TrendRepository TREND_REPOSITORY = new TrendJsonStorage();

    public static void main(String[] args){
        try (ServerController server = ServerController.getInstance()) {
            server.init(20302, USER_REPOSITORY, TREND_REPOSITORY);
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}