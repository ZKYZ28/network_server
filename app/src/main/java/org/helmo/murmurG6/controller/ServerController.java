package org.helmo.murmurG6.controller;

import org.helmo.murmurG6.infrastructure.ServerJsonStorage;
import org.helmo.murmurG6.models.AESCrypt;
import org.helmo.murmurG6.models.Protocol;
import org.helmo.murmurG6.models.User;
import org.helmo.murmurG6.models.UserLibrary;
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
    private final UserRepository storage;
    private final UserLibrary userLibrary;

    /**
     * Le constructeur de la classe ServerController permet de créer un nouveau serveur en spécifiant un numéro de port et un storage d'utilisateurs.
     *
     * @param port Le numéro de port sur lequel le serveur écoutera les connexions entrantes.
     * @param repo Le storage d'utilisateurs qui sera utilisé pour enregistrer et lire les informations d'utilisateur.
     * @throws IOException En cas d'échec de la création du socket serveur.
     */
    public ServerController(int port, UserRepository repo) throws IOException {
        SSLServerSocketFactory sslServerSocketFactory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
        this.serverSocket = (SSLServerSocket) sslServerSocketFactory.createServerSocket(port);
        //this.serverSocket = new ServerSocket(port);
        this.storage = repo;
        this.userLibrary = repo.load(); //remplissage de tous les users inscrits dans la usercollection

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
            //Socket client = serverSocket.accept();
            System.out.println("Quelqu'un s'est connecté!");
            ClientRunnable runnable = new ClientRunnable(client);
            clientList.add(runnable);
            new Thread(runnable).start();
        }
    }

    public void broadcastToAllClientsExceptMe(ClientRunnable me, String message) {
        System.out.printf("[broadcastAll] Message envoyé : %s\n", message);
        for (ClientRunnable c : clientList) {
            if (c != me) {
                c.sendMessage("MSGS admin@192.168.0.19 " + message);
            }
        }
    }


    /*public void broadcastOther(ClientRunnable me, String message) {
        Set<ClientRunnable> needToSend = tellWhoRecevedMessage(me, message);
        for (ClientRunnable c : needToSend) {
            c.sendMessage("MSGS " + c.getUser().getLogin() + "@server1.domain.guru " + message);
        }
    }

    public Set<ClientRunnable> tellWhoRecevedMessage(ClientRunnable me, String message) {
        Set<ClientRunnable> needToSend = Collections.synchronizedSet(new HashSet<>());
        for (ClientRunnable c : clientList) {
            if (c != me) {
                if (c.getUser().chekcIfFollowUser(me.getUser().getLogin())) {
                    needToSend.add(c);
                }
                Set<String> trendsInMessage = detectTrends(message);
                if(trendsInMessage.size() != 0) {
                    for (FollowInformation follow : me.getUser().getFollowedTrends()) {
                        if (c.getUser().chekcIfFollowTrend(follow) && trendsInMessage.contains(follow.getInformationFollow())) {
                            needToSend.add(c);
                        }
                    }
                }
            }
        }
        return needToSend;
    }

    private Set<String> detectTrends(String message) {
        Set<String> trends = new HashSet<>();
        int index = 0;
        while (index < message.length()) {
            index = message.indexOf("#", index);
            if (index == -1) {
                break;
            }
            index++;
            int debutMot = index;
            int finMot = message.indexOf(" ", index);
            if (finMot == -1) {
                finMot = message.length();
            }
            String mot = message.substring(debutMot, finMot);
            trends.add(mot);
        }
        return trends;
    }*/

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
            if (c != senderClient && (c.getUser().followsUser(sender.getLogin()) || c.getUser().followsTrend(extractTrends(message)))) {
                try {
                    c.sendMessage("MSGS " + senderClient.getUser().getLogin()+"@"+getIp() + " " + AESCrypt.encrypt(message, new ServerJsonStorage().loadKeyAes()));
                } catch (ReadServerConfigurationException e) {
                    System.out.println("ERREUR " + e.getMessage());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public void saveUsers() throws SaveUserCollectionException {
        storage.save(this.userLibrary);
    }

    public UserLibrary getUserCollection() {
        return userLibrary;
    }

    public String getIp() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
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