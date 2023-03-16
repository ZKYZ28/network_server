package org.helmo.murmurG6.controller;

import org.helmo.murmurG6.controller.exceptions.UnableToRunClientException;
import org.helmo.murmurG6.executor.Executor;
import org.helmo.murmurG6.models.*;
import org.helmo.murmurG6.models.exceptions.UnableToMatchProtocolException;
import org.helmo.murmurG6.models.exceptions.UserAlreadyRegisteredException;
import org.helmo.murmurG6.repository.exceptions.UnableToSaveTrendLibraryException;
import org.helmo.murmurG6.repository.exceptions.UnableToSaveUserLibraryException;
import org.helmo.murmurG6.utils.RandomSaltGenerator;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashSet;
import java.util.regex.Matcher;

/**
 * La classe ClientRunnable est utilisée pour gérer la communication avec les clients connectés au serveur.
 * Elle implémente l'interface Runnable et est exécutée dans un thread séparé pour chaque client connecté.
 */
public class ClientRunnable implements Runnable, Closeable {

    private final ServerController server;
    private final Socket socket;
    private User user;
    private BufferedReader in;
    private PrintWriter out;

    /**
     * Constructeur de la classe ClientRunnable.
     * Établit une connexion avec le client passé en paramètre et crée un BufferedReader et un PrintWriter pour la communication avec le client.
     *
     * @param socket Socket du client connecté
     */
    public ClientRunnable(Socket socket) {
        this.server = ServerController.getInstance();
        this.socket = socket;
        try {
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            this.out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
        } catch (IOException e) {
            System.out.println("Impossible de créer la connection avec le client.");
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
    @Override
    public void run() throws UnableToRunClientException {
        try {
            //L'executor est un singleton. Le thread connait l'executor via une interface
            TaskScheduler executor = Executor.getInstance();

            //Envoi du message Hello au client + récupération du random de 22 caractères aléatoires
            String random22 = sayHello();

            String ligne = in.readLine();

            while (ligne != null && !ligne.isEmpty() && !socket.isClosed()) {

                System.out.printf("Ligne reçue : %s\r\n", ligne);
                Task task = new Task(server.generateId(), user != null ? user.getCredentials() : null, null, Protocol.detectTaskType(ligne), ligne);
                Matcher params = Protocol.getMatcher(task.getType(), task.getContent());

                switch (task.getType()) {
                    case REGISTER:
                        register(params);
                        break;

                    case CONNECT:
                        connect(params);
                        break;

                    case CONFIRM:
                        confirm(params.group("challenge"), random22);
                        break;

                    case DISCONNECT:
                        close();
                        break;

                    default:
                        executor.addTask(task);
                        break;
                }
                ligne = in.readLine();
            }
        } catch (IOException ex) {
            System.out.println("Impossible de lire la ligne envoyée par leF client.");
        } catch (UnableToMatchProtocolException e) {
            System.out.println("Message non attendu par le Protocol");
        }
    }

    /**
     * Envoie un message au client.
     *
     * @param message Le message à envoyer.
     */
    public void sendMessage(String message) {
        this.out.println(message);
        this.out.flush();
    }


    public User getUser() {
        return this.user;
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
        } catch (UnableToSaveUserLibraryException | UnableToSaveTrendLibraryException | UserAlreadyRegisteredException e) {
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
    private void confirm(String challengeReceived, String random22) {
        String expected = user.getBcrypt().generateChallenge(random22);
        sendMessage(controlConfirm(challengeReceived, expected));
        checkOfflineMessages();
    }

    private String controlConfirm(String clientChallenge, String userChallenge) {
        if(clientChallenge.equals(userChallenge)){
            return Protocol.build_OK();
        }else{
            return Protocol.build_ERROR();
        }
    }


    /**
     * OFFLINE_MESSAGES
     */
    private void checkOfflineMessages(){
        //On regarde si des messages ont été envoyé au client quand celui ci était hors ligne
        if(server.areOfflineMessagesForClient(this)){
            for(OffLineMessage message : server.getOfflineMessagesForClient(this)){
                try{
                    String decryptedMessage = AESCrypt.decrypt(Base64.getDecoder().decode(message.getMessage()), server.getServerConfig().base64KeyAES);
                    sendMessage("MSGS " + message.getSenderCreditentialsInString() + " " + decryptedMessage + " (envoye le: " + message.getDateTime() + ")");
                }catch (Exception e){
                    System.out.println("ERREUR lors du décryptage du message hors ligne");
                }
            }
            server.deleteOfflineMessagesForClient(this);
        }
    }

    @Override
    public void close() throws IOException {
        System.out.println("[ClientRunnable] Déconnection de " + this.user.getLogin());
        sendMessage(Protocol.build_OK());
        this.server.removeClient(this);
        this.in.close();
        this.out.close();
        this.socket.close();
    }
}
