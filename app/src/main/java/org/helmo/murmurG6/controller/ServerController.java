package org.helmo.murmurG6.controller;

import org.helmo.murmurG6.models.User;
import org.helmo.murmurG6.models.UserCollection;
import org.helmo.murmurG6.models.exceptions.UserAlreadyRegisteredException;
import org.helmo.murmurG6.repository.IUserCollectionRepository;
import org.helmo.murmurG6.repository.exceptions.SaveUserCollectionException;
import org.helmo.murmurG6.utils.UltraImportantClass;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * La classe ServerController représente le contrôleur principal de l'application serveur.
 * Cette classe gère la connexion des clients et communique avec la classe ClientRunnable pour gérer la communication avec les clients.
 */
public class ServerController implements AutoCloseable {
    private final List<ClientRunnable> clientList = Collections.synchronizedList(new ArrayList<>());
    private final ServerSocket serverSocket;
    private final IUserCollectionRepository repo;
    private final UserCollection userCollection = new UserCollection();
    private Executor executor;

    /**
     * Le constructeur de la classe ServerController permet de créer un nouveau serveur en spécifiant un numéro de port et un storage d'utilisateurs.
     *
     * @param port Le numéro de port sur lequel le serveur écoutera les connexions entrantes.
     * @param repo Le storage d'utilisateurs qui sera utilisé pour enregistrer et lire les informations d'utilisateur.
     * @throws IOException En cas d'échec de la création du socket serveur.
     */
    public ServerController(int port, IUserCollectionRepository repo) throws IOException {
        this.serverSocket = new ServerSocket(port);
        this.repo = repo;
        this.userCollection.setRegisteredUsers(repo.read()); //remplissage de tous les users inscrits dans la usercollection
        UltraImportantClass.welcome();
        System.out.println("****************************************************************");
        System.out.println("********      SERVER ONLINE ! IP : " +getIp()+"        *********");
        System.out.println("****************************************************************");
    }

    /**
     * La méthode start() démarre le serveur en initialisant l'objet Executor et en écoutant les connexions des clients.
     * Lorsqu'un client se connecte, une nouvelle instance de ClientRunnable est créée pour gérer la communication avec celui-ci.
     *
     * @throws IOException en cas d'erreur lors de l'initialisation du ServerSocket
     */
    public void start() throws IOException {
        this.executor = new Executor(this);
        new Thread(executor).start();
        while(true) {
            Socket client = serverSocket.accept();
            System.out.println("Quelqu'un s'est connecté!");
            ClientRunnable runnable = new ClientRunnable(client, this);
            clientList.add(runnable);
            new Thread(runnable).start();
        }
    }

    public void broadcastToAllClientsExceptMe(ClientRunnable me, String message) {
        System.out.printf("[broadcastAll] Message envoyé : %s\n", message);
        for(ClientRunnable c : clientList) {
            if (c != me) {
                c.sendMessage("MSGS admin@192.168.0.19 "+message);
            }
        }
    }


    /**
     * Enregistre un utilisateur dans la liste d'inscription de User du server.
     * @param user L'utilisateur à inscrire sur le server
     * @throws RegistrationImpossibleException
     */
    public void registerUser(User user) throws RegistrationImpossibleException {
        try{
            userCollection.registerUser(user);
            repo.save(userCollection.getRegisteredUsers().values()); //On sauvegarde le contenu de la userCollection à la fermeture du server
        } catch (UserAlreadyRegisteredException | SaveUserCollectionException e) {
            throw new RegistrationImpossibleException("Inscription impossible!");
        }
    }


    /**
     * Retourne la liste des inscrit sur le server
     * @return un objet UserCollection
     */
    public UserCollection getUserCollection() {
        return userCollection;
    }

    /**
     * Recupere l'addresse IP du server
     * @return L'addresse IP en chaine de carctere
     */
    public String getIp() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Recupere l'executor du server
     * @return un objet Executor
     */
    public Executor getExecutor() {
        return executor;
    }


    /**
     * Le close sert il a quelque chose ici ? car le server tourne à l'infini et se termine à la fin du programme
     */
    @Override
    public void close() {
        try {
            this.serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}