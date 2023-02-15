package org.helmo.murmurG6.controller;

import org.helmo.murmurG6.models.User;
import org.helmo.murmurG6.models.UserLibrary;
import org.helmo.murmurG6.models.exceptions.UserAlreadyRegisteredException;
import org.helmo.murmurG6.repository.UserRepository;
import org.helmo.murmurG6.repository.exceptions.SaveUserCollectionException;
import org.helmo.murmurG6.utils.UltraImportantClass;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.*;

/**
 * La classe ServerController représente le contrôleur principal de l'application serveur.
 * Cette classe gère la connexion des clients et communique avec la classe ClientRunnable pour gérer la communication avec les clients.
 */
public class ServerController implements AutoCloseable {
    private final Set<ClientRunnable> clientList = Collections.synchronizedSet(new HashSet<>());
    private final ServerSocket serverSocket;
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
        this.serverSocket = new ServerSocket(port);
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
            Socket client = serverSocket.accept();
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