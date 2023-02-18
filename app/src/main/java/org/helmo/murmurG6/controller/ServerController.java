package org.helmo.murmurG6.controller;

import org.helmo.murmurG6.infrastructure.ServerJsonStorage;
import org.helmo.murmurG6.models.*;
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
    private final TrendLibrary trendLibrary = new TrendLibrary(); //TODO Executor: quand follow, enregistrer le follower de la trend
    private final ServerConfig serverConfig;

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
        this.storage = repo;
        this.userLibrary = repo.load(); //remplissage de tous les users inscrits dans la usercollection

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

    /**
     * Recupere toute les trend (uniquement leur name) mentionnée dans un message
     * @param message le message ananlysé
     * @return un set de tagname
     */
    private Set<String> extractTrends(String message) {
        Matcher matcher = Pattern.compile(Protocol.TAG).matcher(message);
        HashSet<String> matches = new HashSet<>();
        while (matcher.find()) {
            matches.add(matcher.group()); //Ex: #trend
        }
        return matches;
    }


    /**
     * Cast un message à des utilisateurs locaux ou distant
     * @param senderClient Le thread Client Emetteur du message
     * @param message le message à caster
     */
    public void castMsg(ClientRunnable senderClient, String message) throws Exception {
        //Tactique: on gère d'abord l'envoi des message aux followers du sender, ensuite on gerera les messages aux followers des trends

        System.out.printf("Message envoyé : %s\n", message);
        User sender = senderClient.getUser();
        UUID idMessage = UUID.randomUUID();

        castToFollowers(senderClient, sender.getUserFollowers(), message, idMessage);
        castToTrendFollowers(senderClient, sender.getUserFollowers(), message, idMessage);
    }

    private void castToFollowers(ClientRunnable senderClient, Set<User> followers, String message, UUID idMessage) throws Exception {
        //Parcours de la liste des followers du sender + envoi des message
        for(User follower: followers){

            manageMessageSending(senderClient, message, idMessage, follower);
        }
    }





    private void castToTrendFollowers(ClientRunnable senderClient, Set<User> followers, String message, UUID idMessage) {
        //On parcours les trends du message
        for(String trendName: extractTrends(message)){

            //On regarde si la trend appartient à ce server
            if(trendLibrary.containsKey(trendName)){

                //Si oui, alors on itère sur les followers de cette trends afin de leur écrire
                for(UserCredentials userCredentials: trendLibrary.get(trendName)){

                    manageMessageSending(senderClient, message, idMessage, userCredentials);
                }
            }
        }
    }


    private void manageMessageSending(ClientRunnable senderClient, String message, UUID idMessage, User follower) throws Exception {
        //on regarde si le client est sur ce server ou sur un autre domaine
        if(follower.getDomain().equals(serverConfig.getServerName())){

            //On recupere le threadClient sur le server du follower afin de lui écrire
            ClientRunnable client = getClientRunnableByLogin(follower.getLogin());

            //Si le client est connecté, alors on lui écrit
            if(client != null && !follower.hasAlreadyReceived) {
                client.sendMessage(Protocol.build_MSGS(senderClient.getUser().getLogin() + "@" + getIp() + " " + AESCrypt.encrypt(message, serverConfig.getBase64KeyAES())));
                follower.saveReceivedMessageId(idMessage);
            }else{
                //TODO : Extension file de message quand le client n'est pas connecté (quand il est nul dans ce cas ci)
            }
        }else{
            relay.sendMessage(Protocol.build_SEND(idMessage.toString(), senderClient.getUser().getLogin(), follower.getLogin(), message));
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

    public ServerConfig getServerConfig() {
        return serverConfig;
    }



    private ClientRunnable getClientRunnableByLogin(String login){
        for(ClientRunnable cr : clientList){
            if(cr.getUser().getLogin().equals(login)){
                return cr;
            }
        }
        return null;
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