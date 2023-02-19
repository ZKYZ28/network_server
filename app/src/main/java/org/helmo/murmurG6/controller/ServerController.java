package org.helmo.murmurG6.controller;

import org.helmo.murmurG6.controller.exceptions.UnableToConnectToClientException;
import org.helmo.murmurG6.controller.exceptions.UnableToExecuteTaskException;
import org.helmo.murmurG6.controller.exceptions.UnableToRunClientException;
import org.helmo.murmurG6.executor.Executor;
import org.helmo.murmurG6.infrastructure.ServerJsonStorage;
import org.helmo.murmurG6.models.*;
import org.helmo.murmurG6.repository.TrendRepository;
import org.helmo.murmurG6.repository.UserRepository;
import org.helmo.murmurG6.repository.exceptions.UnableToLoadTrendLibraryException;
import org.helmo.murmurG6.repository.exceptions.UnableToLoadUserLibraryException;
import org.helmo.murmurG6.repository.exceptions.UnableToSaveTrendLibraryException;
import org.helmo.murmurG6.repository.exceptions.UnableToSaveUserLibraryException;
import org.helmo.murmurG6.utils.UltraImportantClass;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * La classe ServerController représente le contrôleur principal de l'application serveur.
 * Cette classe gère la connexion des clients et communique avec la classe ClientRunnable pour gérer la communication avec les clients.
 */
public class ServerController implements AutoCloseable {

    private static ServerController instance;
    private final Set<ClientRunnable> clientList = Collections.synchronizedSet(new HashSet<>());
    private SSLServerSocket serverSocket;
    private ServerConfig serverConfig;


    //TODO Déplacer ces 4 attribut dans une autre classe
    private UserRepository userRepository;
    private TrendRepository trendRepository;
    private UserLibrary userLibrary;
    private TrendLibrary trendLibrary; //TODO Executor: quand follow, enregistrer le follower de la trend


    public static ServerController getInstance() {
        if (instance == null) {
            instance = new ServerController();
        }
        return instance;
    }

    public void init(int port, UserRepository userRepository, TrendRepository trendRepository) {
        try{
            SSLServerSocketFactory sslServerSocketFactory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
            this.serverSocket = (SSLServerSocket) sslServerSocketFactory.createServerSocket(port);

            this.serverConfig = new ServerJsonStorage().load();
            this.serverConfig.setServerIp(getDomain());

            this.userRepository = userRepository;
            this.trendRepository = trendRepository;

            this.userLibrary = userRepository.load();
            this.trendLibrary = trendRepository.load();

        } catch (IOException e) {
            e.printStackTrace(); //TODO Faire des execption explicites
        }
    }


    public void start() throws IOException {
        welcome();
        try {
            TaskScheduler executor = Executor.getInstance();
            new Thread(executor).start();
            new Thread(new ServerAnnouncer(this, "224.1.1.255", 23106)).start();

            while (!this.serverSocket.isClosed()) {
                SSLSocket client = (SSLSocket) serverSocket.accept();
                System.out.println("Quelqu'un s'est connecté!");
                ClientRunnable runnable = new ClientRunnable(client);
                clientList.add(runnable);
                new Thread(runnable).start();
            }
        } catch (UnableToConnectToClientException | UnableToRunClientException | UnableToExecuteTaskException e) {
            e.printStackTrace();
        }
    }

    private void welcome() {
        UltraImportantClass.welcome();
        System.out.println("**************************************************************************************");
        System.out.println("********     SERVER ONLINE ! IP : " + getDomain() + " " + serverSocket.getInetAddress().getHostAddress() + "     ********");
        System.out.println("**************************************************************************************");
    }


    public boolean isRunning() {
        return !this.serverSocket.isClosed();
    }

    public void save() throws UnableToSaveUserLibraryException, UnableToSaveTrendLibraryException {
        userRepository.save(this.userLibrary);
        trendRepository.save(this.trendLibrary);
    }


    @Override
    public void close() {
        try {
            this.serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /************** GETTERS/SETTERS ***************/
    public UserLibrary getUserLibrary() {
        return userLibrary;
    }

    public TrendLibrary getTrendLibrary() {
        return trendLibrary;
    }

    public String getDomain() {
        try {
            return InetAddress.getLocalHost().getCanonicalHostName();
        } catch (UnknownHostException e) {
            return null;
        }
    }

    public ServerConfig getServerConfig() {
        return serverConfig;
    }

    public Set<ClientRunnable> getClientList() {
        return this.clientList;
    }
}