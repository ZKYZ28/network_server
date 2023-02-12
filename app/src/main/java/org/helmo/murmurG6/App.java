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
    private static final int DEFAULT_PORT = 12345;
    private static final UserJsonStorage USER_JSON_STORAGE = new UserJsonStorage();

    public static void main(String[] args){
            try (ServerController server = new ServerController(DEFAULT_PORT, getSSLServerSocketFactory(), USER_JSON_STORAGE)) {
                server.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    public static SSLServerSocketFactory getSSLServerSocketFactory(){
        try {
            KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());

            try(InputStream certif = new FileInputStream(new File("app/src/main/resources/", "star.godswila.guru.p12"))){
                ks.load(certif, "labo2023".toCharArray());
            }

            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(ks, "labo2023".toCharArray());

            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(ks);

            SSLContext sc = SSLContext.getInstance("TLS");
            TrustManager[] trustManagers = tmf.getTrustManagers();
            sc.init(kmf.getKeyManagers(), trustManagers, null);

            return sc.getServerSocketFactory();
        }catch (KeyStoreException e) {
            System.out.println("1");
            throw new RuntimeException(e);
        } catch (UnrecoverableKeyException e) {
            System.out.println("2");
            throw new RuntimeException(e);
        } catch (FileNotFoundException e) {
            System.out.println("3");
            throw new RuntimeException(e);
        } catch (CertificateException e) {
            System.out.println("4");
            throw new RuntimeException(e);
        } catch (IOException e) {
            System.out.println("5");
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            System.out.println("6");
            throw new RuntimeException(e);
        } catch (KeyManagementException e) {
            System.out.println("7");
            throw new RuntimeException(e);
        }
    }
}

    //python main.py 192.168.1.63 12345 admin admin