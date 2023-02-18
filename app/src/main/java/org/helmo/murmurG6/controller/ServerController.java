package org.helmo.murmurG6.controller;

import org.helmo.murmurG6.infrastructure.ServerJsonStorage;
import org.helmo.murmurG6.models.*;
import org.helmo.murmurG6.repository.TrendRepository;
import org.helmo.murmurG6.repository.UserRepository;
import org.helmo.murmurG6.repository.exceptions.ReadServerConfigurationException;
import org.helmo.murmurG6.repository.exceptions.SaveUserCollectionException;
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
    private final Set<ClientRunnable> clientList = Collections.synchronizedSet(new HashSet<>());
    private final SSLServerSocket serverSocket;
    private final UserRepository userRepository;
    private final TrendRepository trendRepository;
    private final UserLibrary userLibrary;
    private final TrendLibrary trendLibrary;
    private final ServerConfig serverConfig;

    /**
     * Le constructeur de la classe ServerController permet de créer un nouveau serveur en spécifiant un numéro de port et un storage d'utilisateurs.
     *
     * @param port Le numéro de port sur lequel le serveur écoutera les connexions entrantes.
     * @param userRepository Le storage d'utilisateurs qui sera utilisé pour enregistrer et lire les informations d'utilisateur.
     * @throws IOException En cas d'échec de la création du socket serveur.
     */
    public ServerController(int port, UserRepository userRepository, TrendRepository trendRepository) throws IOException {
        SSLServerSocketFactory sslServerSocketFactory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
        this.serverSocket = (SSLServerSocket) sslServerSocketFactory.createServerSocket(port);
        this.userRepository = userRepository;
        this.trendRepository = trendRepository;
        this.userLibrary = userRepository.load(); //remplissage de tous les users inscrits dans la usercollection
        this.trendLibrary = trendRepository.load();

        this.serverConfig = new ServerJsonStorage().loadServerConfiguration();
        this.serverConfig.setServerIp(getIp());

        UltraImportantClass.welcome();
        System.out.println("****************************************************************");
        System.out.println("********      SERVER ONLINE ! IP : " + getIp() + "        *********");
        System.out.println("****************************************************************");
    }


    public void start() throws IOException {
        TaskScheduler executor = Executor.getInstance();
        executor.setServer(this);
        new Thread(executor).start();

        while (!this.serverSocket.isClosed()) {
            SSLSocket client = (SSLSocket) serverSocket.accept();
            System.out.println("Quelqu'un s'est connecté!");
            ClientRunnable runnable = new ClientRunnable(client);
            clientList.add(runnable);
            new Thread(runnable).start();
        }
    }

    private Set<String> extractTrends(String message) {
        Pattern pattern = Pattern.compile(Protocol.TAG);
        Matcher matcher = pattern.matcher(message);
        HashSet<String> matches = new HashSet<>();

        while (matcher.find()) {
            matches.add(matcher.group()); //Ex: #trend
        }

        return matches;
    }

    //TODO
    public void castMsg(ClientRunnable senderClient, String message) {
        System.out.printf("Message envoyé : %s\n", message);
        User sender = senderClient.getUser();

        for (ClientRunnable c : clientList) {
            try {
                sender.getUserFollowers().contains(c.getUser());

                /*if (c != senderClient && (c.getUser().followsUser(sender.getLogin() + "@" + serverConfig.getServerName())) || c.getUser().followsTrend(extractTrends(message))) {  //TODO A CHANGER LA PARTIE DES TRENDS
                    c.sendMessage(Protocol.build_MSGS(senderClient.getUser().getLogin() + "@" + getIp() + " " + AESCrypt.encrypt(message, serverConfig.getBase64KeyAES())));
                } else if (c != senderClient && (c.getUser().followsUser("") || c.getUser().followsTrend(extractTrends(message)))) {  //TODO A CHANGER LA PARTIE DES TRENDS ET DU USER
                    c.sendMessage(Protocol.build_SEND("","","",""));  //TODO A CHANGER QUAND ON VEUT SEND
                }*/

            } catch (ReadServerConfigurationException e) {
                System.out.println("ERREUR LORS DE L'ENVOIE D'UN MESSAGE" + e.getMessage());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void save() throws SaveUserCollectionException {
        userRepository.save(this.userLibrary);
        trendRepository.save(this.trendLibrary);
    }

    public UserLibrary getUserLibrary() {
        return userLibrary;
    }

    public TrendLibrary getTrendLibrary() {
        return trendLibrary;
    }

    public String getIp() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    public ServerConfig getServerConfig() {
        return serverConfig;
    }

    @Override
    public void close() {
        try {
            this.serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}