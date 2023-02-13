package org.helmo.murmurG6;

import org.helmo.murmurG6.infrastructure.UserJsonStorage;
import org.helmo.murmurG6.system.ServerController;

import javax.net.ServerSocketFactory;
import javax.net.ssl.*;
import java.io.*;
import java.nio.file.Files;
import java.security.*;
import java.security.cert.CertificateException;

public class App {
    private static final int DEFAULT_PORT = 23106;
    private static final UserJsonStorage USER_JSON_STORAGE = new UserJsonStorage();

    static {
        System.setProperty("javax.net.ssl.keyStore", "app/src/main/resources/star.godswila.guru.p12");
        System.setProperty("javax.net.ssl.keyStorePassword", "labo2023");
    }

    public static void main(String[] args){
            try (ServerController server = new ServerController(DEFAULT_PORT, USER_JSON_STORAGE)) {
                server.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
    }
}

    //python main.py 192.168.1.63 12345 admin admin