package org.helmo.murmurG6.controller;

import org.helmo.murmurG6.controller.exceptions.UnableToConnectToClientException;
import org.helmo.murmurG6.controller.exceptions.UnableToRunClientException;
import org.helmo.murmurG6.executor.Executor;
import org.helmo.murmurG6.models.*;
import org.helmo.murmurG6.models.exceptions.UserAlreadyRegisteredException;
import org.helmo.murmurG6.repository.exceptions.UnableToSaveTrendLibraryException;
import org.helmo.murmurG6.repository.exceptions.UnableToSaveUserLibraryException;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.regex.Matcher;

/**
 * La classe ClientRunnable est utilisée pour gérer la communication avec les clients connectés au serveur.
 * Elle implémente l'interface Runnable et est exécutée dans un thread séparé pour chaque client connecté.
 */
public class ClientRunnable implements Runnable {
    private final BufferedReader in;
    private final PrintWriter out;
    private User user;
    private String random22;
    ServerController server = ServerController.getInstance();


    /**
     * Constructeur de la classe ClientRunnable.
     * Établit une connexion avec le client passé en paramètre et crée un BufferedReader et un PrintWriter pour la communication avec le client.
     *
     * @param client Socket du client connecté
     * @throws UnableToConnectToClientException si la connexion au client échoue
     */
    public ClientRunnable(Socket client) throws UnableToConnectToClientException {
        try {
            in = new BufferedReader(new InputStreamReader(client.getInputStream(), StandardCharsets.UTF_8));
            out = new PrintWriter(new OutputStreamWriter(client.getOutputStream(), StandardCharsets.UTF_8), true);
        } catch (IOException ex) {
            throw new UnableToConnectToClientException("Impossible de se connecter au client. Une erreur est survenue lors de la création des canaux de communication pour le thread client. Vérifiez que le client est correctement connecté et que les ports de communication sont disponibles.", ex);
        }
    }

    /**
     * Méthode run de l'interface Runnable.
     * Cette méthode est exécutée dans un thread séparé pour chaque client connecté.
     * Elle lit les messages envoyés par le client, crée des tâches en fonction des messages reçus, et les ajoute à la file d'attente du TaskScheduler (Executor).
     * Si une exception est levée, la méthode lève une UnableToRunClientException.
     *
     * @throws UnableToRunClientException si une erreur se produit lors de la communication avec le client
     */
    //TODO RENDRE LES METHODES SERVER SYNCHRONIZED CAR PLUSIEURS CLIENTS PEUVENT LES EXECUTER !!!!!!!!!!!!
    public void run() throws UnableToRunClientException {
        try {
            //L'executor est un singleton. Le thread connait l'executor via une interface
            TaskScheduler executor = Executor.getInstance();

            //Envoi du message Hello au client + récupération du random de 22 caractères aléatoires
            random22 = executor.sayHello(this);



            String ligne = in.readLine();

            while (ligne != null && !ligne.isEmpty()) {
                System.out.printf("Ligne reçue : %s\r\n", ligne);

                Task task = new Task(
                        server.generateId(),
                        this,
                        user != null ? user.getCredentials() : null,
                        null,
                        Protocol.detectTaskType(ligne),
                        ligne
                );

                Matcher params = Protocol.getMatcher(task.getType(), task.getContent());

                if(params != null){
                    switch (task.getType()) {
                        case REGISTER:
                            register(params);
                            break;

                        case CONNECT:
                            connect(params);
                            break;

                        case CONFIRM:
                            confirm(params.group("challenge"));
                            break;

                        case DISCONNECT:
                            server.removeClient(this);
                            break;

                        default:
                            executor.addTask(task);
                            break;
                    }
                }else{
                    sendMessage(Protocol.build_ERROR());
                }


                ligne = in.readLine();
            }
        } catch (IOException ex) {
            throw new UnableToRunClientException("Une erreur est survenue lors de la lecture du socket.", ex);
        }
    }

    /**
     * Envoie un message au client.
     *
     * @param message Le message à envoyer.
     */
    public void sendMessage(String message) {
        out.println(message);
        out.flush();
    }


    /***************** GETTERS/SETTERS *****************/


    public User getUser() {
        return this.user;
    }

    public void setUser(User u) {
        this.user = u;
    }

    public String getRandom22() {
        return random22;
    }

    /*************************************************/



    /** REGISTER **/
    private void register(Matcher params){
        User user = new User(
                new UserCredentials(params.group("username"), server.getServerConfig().getServerName()),
                BCrypt.of(params.group("bcrypt")),
                new HashSet<>(),
                new HashSet<>());

        this.sendMessage(executeRegister(user));
    }

    private String executeRegister(User user) {
        try {
            server.getUserLibrary().register(user);
            setUser(user);
            server.save();
            return Protocol.build_OK();
        } catch (UnableToSaveUserLibraryException | UnableToSaveTrendLibraryException | UserAlreadyRegisteredException e) {
            return Protocol.build_ERROR();
        }
    }


    /** CONNECT **/
    private void connect(Matcher params){
        sendMessage(controlConnect(params.group("username")));
    }

    private String controlConnect(String login) {
        if (server.getUserLibrary().isRegistered(login)) {
            User user = server.getUserLibrary().getUser(login);
            setUser(user);
            return Protocol.build_PARAM(user.getBcryptRound(), user.getBcryptSalt());
        } else {
            return Protocol.build_ERROR();
        }
    }


    /** CONFIRM **/
    private void confirm(String challengeReceived){
        String expected = user.getBcrypt().generateChallenge(getRandom22());
        sendMessage(controlConfirm(challengeReceived, expected));
    }

    private String controlConfirm(String clientChallenge, String userChallenge) {
        return clientChallenge.equals(userChallenge) ? Protocol.build_OK() : Protocol.build_ERROR();
    }
}
