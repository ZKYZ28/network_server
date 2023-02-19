package org.helmo.murmurG6.controller;

import org.helmo.murmurG6.controller.exceptions.UnableToConnectToClientException;
import org.helmo.murmurG6.controller.exceptions.UnableToExecuteTaskException;
import org.helmo.murmurG6.controller.exceptions.UnableToRunClientException;
import org.helmo.murmurG6.infrastructure.ServerJsonStorage;
import org.helmo.murmurG6.models.*;
import org.helmo.murmurG6.repository.TrendRepository;
import org.helmo.murmurG6.repository.UserRepository;
import org.helmo.murmurG6.repository.exceptions.UnableToLoadServerConfigurationException;
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
    private final Set<ClientRunnable> clientList = Collections.synchronizedSet(new HashSet<>());
    private final SSLServerSocket serverSocket;
    private final UserRepository userRepository;
    private final TrendRepository trendRepository;
    private final UserLibrary userLibrary;
    private final TrendLibrary trendLibrary; //TODO Executor: quand follow, enregistrer le follower de la trend
    private final ServerConfig serverConfig;

    /**
     * Le constructeur de la classe ServerController permet de créer un nouveau serveur en spécifiant un numéro de port et un storage d'utilisateurs.
     *
     * @param port            Le numéro de port sur lequel le serveur écoutera les connexions entrantes.
     * @param userRepository  Le storage d'utilisateurs qui sera utilisé pour enregistrer et lire les informations d'utilisateur.
     * @param trendRepository Le storage de tendances qui sera utilisé pour enregistrer et lire les informations de tendances.
     * @throws IOException En cas d'échec de la création du socket serveur.
     */
    public ServerController(int port, UserRepository userRepository, TrendRepository trendRepository) throws IOException {
        SSLServerSocketFactory sslServerSocketFactory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
        this.serverSocket = (SSLServerSocket) sslServerSocketFactory.createServerSocket(port);

        this.userRepository = userRepository;
        this.trendRepository = trendRepository;

        this.userLibrary = userRepository.load();
        this.trendLibrary = trendRepository.load();

        this.serverConfig = new ServerJsonStorage().load();
        this.serverConfig.setServerIp(getDomain());

        UltraImportantClass.welcome();
        System.out.println("****************************************************************");
        System.out.println("********     SERVER ONLINE ! IP : " + getDomain() + "     ********");
        System.out.println("****************************************************************");
    }


    public void start() throws IOException {
        try {
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
        } catch (UnableToConnectToClientException | UnableToRunClientException | UnableToExecuteTaskException e) {
            e.printStackTrace();
        }
    }




    /**
     * Cast un message à des utilisateurs locaux ou distant
     * @param sender Le thread Client Emetteur du message
     * @param message le message à caster
     */
    public void castMsg(User sender, String message) {
        //Tactique: on gère d'abord l'envoi des message aux followers du sender, ensuite on gerera les messages aux followers des trends

        System.out.printf("Message envoyé : %s\n", message);
        UUID idMessage = UUID.randomUUID();

        //Envoi du message au followers de l'emmetteur du message
        castToFollowers(sender, sender.getUserFollowers(), message, idMessage);

        //Si le message fait mention d'au moins 1 trend -> envoi des message aux abonnés de la trend
        if(extractTrends(message).size() > 0){
            castToTrendFollowers(sender, extractTrends(message), message, idMessage);
        }
    }

    /**
     * Gère le casting de message pour à des followers d'un user
     * @param senderClient L'emetteur du message
     * @param followers La liste des creditentials des followers (ex: antho123@server1)
     * @param message Le message à caster
     * @param idMessage L'id unique associé à ce message
     */
    private void castToFollowers(User senderClient, Set<UserCredentials> followers, String message, UUID idMessage) {
        //Parcours de la liste des followers du sender + envoi des message
        for(UserCredentials followerCreditential: followers){
            manageMessageSending(senderClient, followerCreditential, idMessage, message);
        }
    }


    /**
     * Gère le casting de message pour à des followers d'une Trend
     * @param senderClient L'emetteur du message
     * @param followers La liste des creditentials des followers de la trend(ex: antho123@server1)
     * @param message Le message à caster
     * @param idMessage L'id unique associé à ce message
     */
    private void castToTrendFollowers(User senderClient, Set<String> trends, String message, UUID idMessage) {
        //On parcours les trends mentionné dans le message
        for(String trendName: trends){

            //On regarde si la trend appartient à ce server
            if(trendLibrary.containsKey(trendName)){

                //Si oui, alors on itère sur les followers de cette trends afin de leur écrire
                for(UserCredentials trendFollowersCredential: trendLibrary.get(trendName)){
                    manageMessageSending(senderClient, trendFollowersCredential, idMessage, message);
                }


            //Si non (cas ou la trend n'appartient PAS à ce server) -> on passe la trend au relay
            }else{
                relay.sendMessage(Protocol.build_SEND(idMessage.toString(), senderClient.getLogin(), trendLibrary.get(trendName).toString(), message));
            }
        }
    }


    private void manageMessageSending(User sender, UserCredentials followerCredential, UUID idMessage, String message) {
        //on regarde si le destinataire est sur ce server ou sur un autre domaine
        if(followerCredential.getDomain().equals(serverConfig.getServerName())){

            //On recupere le threadClient sur le server du destinataire afin de lui écrire
            ClientRunnable client = getClientRunnableByLogin(followerCredential.getLogin());

            //Gere l'envoi du message en local (aux user de CE server)
            operateLocalMessageSend(sender, idMessage, message, client);


            //Si le destinataire n'appartient pas à ce server
        }else{
            relay.sendMessage(Protocol.build_SEND(idMessage.toString(), sender.getCreditential.toString(), followerCredential.toString(), message));
        }
    }


    /**
     * Gère l'envoi du message localement (aux utilisateur de CE server)
     * @param sender L'emetteur du message
     * @param idMessage L'id du message
     * @param message Le message
     * @param client Le thread ClientRunnable du destinataire sur ce server
     */
    private void operateLocalMessageSend(User sender, UUID idMessage, String message, ClientRunnable client) {
        //Si le client est connecté
        if(client != null) {

            //On recupere l'objet User associé à ce thread client afin de pourvoir gérer son historique
            User follower = client.getUser();

            //Si le destinataire n'a pas déja recu ce message, alors on lui écrit et enregistre cet événement dans son historique
            if(!follower.hasAlreadyReceived)
                try {
                    client.sendMessage(Protocol.build_MSGS(sender.getLogin() + "@" + getIp() + " " + AESCrypt.encrypt(message, serverConfig.getBase64KeyAES())));
                    follower.saveReceivedMessageId(idMessage);
                }catch (Exception e){
                    System.out.println("Erreur lors de l'envoi du message (encryption error)");
                }

        //Si le destinataire n'est pas actuellement connecté
        }else{
            //TODO : Extension file de message quand le client n'est pas connecté (quand il est nul dans ce cas ci)
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
     * Recupere un objet ClientRunnable en fonction du login (ex: antho123) passé en paramètre
     * @param login le login du client recherché
     * @return Un ClientRunnable si le client est bien trouvé dans la liste des clients connecté du server, null sinon
     */
    private ClientRunnable getClientRunnableByLogin(String login){
        for(ClientRunnable cr : clientList){
            if(cr.getUser().getLogin().equals(login)){
                return cr;
            }
        }
        return null;
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

    private static class MessageEncryptionException extends Exception {
        public MessageEncryptionException(String message) {
            super(message);
        }
    }
}