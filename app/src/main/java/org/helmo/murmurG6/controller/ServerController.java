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
    private final MessageHistoryBuffer messageHistoryBuffer = new MessageHistoryBuffer();

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

    //TODO
    public void castMsg(ClientRunnable senderClient, String message) throws Exception {
        System.out.printf("Message envoyé : %s\n", message);
        User sender = senderClient.getUser();



       //1. Si message contient au moins 1 trend ET que ce trend est follow par des User d'un autre server -> Historique
       //car il y a un risque qu'une personne recoive deux fois le message (follow le sender ET soit abonnée à la trend)
       if(extractTrends(message).size() > 0){


           //Mise en place de l'historique pour palier le risque de message double

           //Generation de l'id du message qui va etre envoyé
           UUID messageID = UUID.randomUUID();
           messageHistoryBuffer.createMessageHistory(messageID);

            //On envoi d'abord le message aux follower LOCAL!!! du sender et on ajoute ceux ci dans l'historique
           for(UserCredentials follower: sender.getUserFollowers()){
               ClientRunnable client = getClientRunnableByLogin(follower.toString());

               //Si le follower est connecté et est un follower local: on lui envoi le msg + on l'ajoute à l'historique
               if(client != null){
                   if(follower.getDomain().equals(serverConfig.getServerName())){
                       client.sendMessage(Protocol.build_MSGS(senderClient.getUser().getLogin() + "@" + getIp() + " " + AESCrypt.encrypt(message, serverConfig.getBase64KeyAES())));
                       messageHistoryBuffer.addUsertoHistory(messageID, follower.getLogin());
                   }
               }
           }


           //On traite les message par rapport aux trends
           for(String trend: extractTrends(message)){

               //Cas ou la trend appartient au server
               if(trendLibrary.containsKey(trend)){

                   //Le server envoi les message aux follower de la trend
                   for(UserCredentials follower: trendLibrary.get(trend)){
                       ClientRunnable client = getClientRunnableByLogin(follower.toString());

                       if(client != null){
                           //on regarde si le client est sur ce server ou sur un autre domaine
                           if(follower.getDomain().equals(serverConfig.getServerName())){

                               //On regarde si le client a deja recu le message (si il est deja present dans l'historique de ce message
                               if(!messageHistoryBuffer.hasAlreadyReceived(messageID, follower.toString())){
                                   client.sendMessage(Protocol.build_MSGS(senderClient.getUser().getLogin() + "@" + getIp() + " " + AESCrypt.encrypt(message, serverConfig.getBase64KeyAES())));
                                   messageHistoryBuffer.addUsertoHistory(messageID, follower.getLogin());
                               }
                           }else{
                               relay.sendMessage(Protocol.build_SEND("","","",""));
                           }
                       }
                   }



               //Cas ou la trend n'appartient pas au server
               }else{
                   relay.sendMessage(Protocol.build_SEND("","","",""));
               }
           }






       //Cas ou il n'y a pas de trend, le sender cast ses followers (pas de risque de double message)
       }else{

           //Parcours de la liste des followers du sender + envoi des message
           for(UserCredentials follower: sender.getUserFollowers()){
               ClientRunnable client = getClientRunnableByLogin(follower.toString());

               if(client != null){
                   //on regarde si le client est sur ce server ou sur un autre domaine
                   if(follower.getDomain().equals(serverConfig.getServerName())){
                       client.sendMessage(Protocol.build_MSGS(senderClient.getUser().getLogin() + "@" + getIp() + " " + AESCrypt.encrypt(message, serverConfig.getBase64KeyAES())));
                   }else{
                       relay.sendMessage(Protocol.build_SEND("","","",""));
                   }
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