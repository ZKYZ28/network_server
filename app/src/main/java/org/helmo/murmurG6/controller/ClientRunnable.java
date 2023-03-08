package org.helmo.murmurG6.controller;

import org.helmo.murmurG6.controller.exceptions.UnableToRunClientException;
import org.helmo.murmurG6.executor.Executor;
import org.helmo.murmurG6.models.*;
import org.helmo.murmurG6.models.exceptions.UserAlreadyRegisteredException;
import org.helmo.murmurG6.repository.exceptions.UnableToSaveTrendLibraryException;
import org.helmo.murmurG6.repository.exceptions.UnableToSaveUserLibraryException;
import org.helmo.murmurG6.utils.RandomSaltGenerator;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.regex.Matcher;

/**
 * La classe ClientRunnable est utilisée pour gérer la communication avec les clients connectés au serveur.
 * Elle implémente l'interface Runnable et est exécutée dans un thread séparé pour chaque client connecté.
 */
public class ClientRunnable implements Runnable, Closeable {

    private User user;
    private String random22;
    private final ServerController server;
    private final Socket socket;


    /**
     * Constructeur de la classe ClientRunnable.
     * Établit une connexion avec le client passé en paramètre et crée un BufferedReader et un PrintWriter pour la communication avec le client.
     *
     * @param socket Socket du client connecté
     */
    public ClientRunnable(Socket socket) {
        this.server = ServerController.getInstance();
        this.socket = socket;
    }

    /**
     * Méthode run de l'interface Runnable.
     * Cette méthode est exécutée dans un thread séparé pour chaque client connecté.
     * Elle lit les messages envoyés par le client, crée des tâches en fonction des messages reçus, et les ajoute à la file d'attente du TaskScheduler (Executor).
     * Si une exception est levée, la méthode lève une UnableToRunClientException.
     *
     * @throws UnableToRunClientException si une erreur se produit lors de la communication avec le client
     */
    @Override
    public void run() throws UnableToRunClientException {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(this.socket.getInputStream(), StandardCharsets.UTF_8))) {

            //L'executor est un singleton. Le thread connait l'executor via une interface
            TaskScheduler executor = Executor.getInstance();

            //Envoi du message Hello au client + récupération du random de 22 caractères aléatoires
            random22 = sayHello();

            String ligne = in.readLine();

            while (!this.socket.isClosed() && ligne != null) {
                System.out.printf("Ligne reçue : %s\r\n", ligne);

                Task task = new Task(server.generateId(), this, user != null ? user.getCredentials() : null, null, Protocol.detectTaskType(ligne), ligne);

                Matcher params = Protocol.getMatcher(task.getType(), task.getContent());

                if (params != null) {
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
                            this.close();
                            break;

                        default:
                            executor.addTask(task);
                            break;
                    }
                } else {
                    sendMessage(Protocol.build_ERROR());
                }

                ligne = in.readLine();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new UnableToRunClientException("Une erreur est survenue lors de la lecture du socket.", ex);
        }
    }

    /**
     * Envoie un message au client.
     *
     * @param message Le message à envoyer.
     */
    public void sendMessage(String message) {
        try (PrintWriter out = new PrintWriter(new OutputStreamWriter(this.socket.getOutputStream(), StandardCharsets.UTF_8), true)) {
            out.println(message);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
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


    /**
     * REGISTER
     **/
    private void register(Matcher params) {
        User user = new User(new UserCredentials(params.group("username"), server.getServerConfig().serverDomain), BCrypt.of(params.group("bcrypt")), new HashSet<>(), new HashSet<>());

        this.sendMessage(executeRegister(user));
    }

    private String executeRegister(User user) {
        try {
            server.getUserLibrary().register(user);
            this.user = user;
            server.save();
            return Protocol.build_OK();
        } catch (UnableToSaveUserLibraryException | UnableToSaveTrendLibraryException |
                 UserAlreadyRegisteredException e) {
            return Protocol.build_ERROR();
        }
    }


    /**
     * CONNECT
     **/
    private void connect(Matcher params) {
        sendMessage(controlConnect(params.group("username")));
    }

    private String controlConnect(String login) {
        if (server.getUserLibrary().isRegistered(login)) {
            User user = server.getUserLibrary().getUser(login);
            this.user = user;
            return Protocol.build_PARAM(user.getBcryptRound(), user.getBcryptSalt());
        } else {
            return Protocol.build_ERROR();
        }
    }

    private String sayHello() {
        String random22 = RandomSaltGenerator.generateSalt();
        sendMessage(Protocol.build_HELLO(server.getServerConfig().serverDomain, random22));
        return random22;
    }

    /**
     * CONFIRM
     **/
    private void confirm(String challengeReceived) {
        String expected = user.getBcrypt().generateChallenge(getRandom22());
        sendMessage(controlConfirm(challengeReceived, expected));
    }

    private String controlConfirm(String clientChallenge, String userChallenge) {
        return clientChallenge.equals(userChallenge) ? Protocol.build_OK() : Protocol.build_ERROR();
    }

    @Override
    public void close() {
        try {
            this.socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
