package org.helmo.murmurG6;

import org.helmo.murmurG6.infrastructure.UserJsonStorage;
import org.helmo.murmurG6.system.ServerController;

import javax.net.ServerSocketFactory;
import javax.net.ssl.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;

public class App {

    /*
     * Chargement des propriétés SSL dans un bloc static afin d'avoir ces propriétés
     * pour toute l'instance de l'application
     */
    static {
        System.setProperty("javax.net.ssl.keyStore", "org/helmo/murmurG6/ssl/star.godswila.guru.p12");
        System.setProperty("javax.net.ssl.keyStorePassword", "labo2023");
    }

    private static final int DEFAULT_PORT = 12345;
    private static final UserJsonStorage USER_JSON_STORAGE = new UserJsonStorage();

    public static void main(String[] args){
        KeyStore ks = null;
        SSLServerSocketFactory ssf = null;
        try {
            ks = KeyStore.getInstance("JKS");
            ks.load(new FileInputStream("keystoreFile"), "keystorePassword".toCharArray());

            KeyManagerFactory kmf = KeyManagerFactory.getInstance("X509");
            kmf.init(ks, "keystorePassword".toCharArray());

            TrustManagerFactory tmf = TrustManagerFactory.getInstance("X509");
            tmf.init(ks);

            SSLContext sc = SSLContext.getInstance("TLS");
            TrustManager[] trustManagers = tmf.getTrustManagers();
            sc.init(kmf.getKeyManagers(), trustManagers, null);

            ssf = sc.getServerSocketFactory();

            try (ServerController server = new ServerController(DEFAULT_PORT, ssf, USER_JSON_STORAGE)) {
                server.start();
            } catch (IOException e) {
                e.printStackTrace();
            }

        } catch (KeyStoreException e) {
            throw new RuntimeException(e);
        } catch (UnrecoverableKeyException e) {
            throw new RuntimeException(e);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (CertificateException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (KeyManagementException e) {
            throw new RuntimeException(e);
        }
    }
}

    //python main.py 192.168.1.63 12345 admin admin