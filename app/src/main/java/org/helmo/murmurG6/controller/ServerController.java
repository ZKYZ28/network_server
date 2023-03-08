package org.helmo.murmurG6.controller;

import org.helmo.murmurG6.controller.exceptions.UnableToConnectToClientException;
import org.helmo.murmurG6.controller.exceptions.UnableToExecuteTaskException;
import org.helmo.murmurG6.controller.exceptions.UnableToRunClientException;
import org.helmo.murmurG6.executor.Executor;
import org.helmo.murmurG6.infrastructure.ServerJsonStorage;
import org.helmo.murmurG6.models.*;
import org.helmo.murmurG6.repository.OffLineMessageRepository;
import org.helmo.murmurG6.repository.TrendRepository;
import org.helmo.murmurG6.repository.UserRepository;
import org.helmo.murmurG6.repository.exceptions.UnableToSaveOffLineMessageLibraryException;
import org.helmo.murmurG6.repository.exceptions.UnableToSaveTrendLibraryException;
import org.helmo.murmurG6.repository.exceptions.UnableToSaveUserLibraryException;
import org.helmo.murmurG6.utils.UltraImportantClass;

import javax.net.ServerSocketFactory;
import javax.net.SocketFactory;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

/**
 * La classe ServerController représente le contrôleur principal de l'application serveur.
 * Cette classe gère la connexion des clients et communique avec la classe ClientRunnable pour gérer la communication avec les clients.
 */
public class ServerController implements AutoCloseable {

    private static ServerController instance;
    private final Set<ClientRunnable> clientList = Collections.synchronizedSet(new HashSet<>());
    private ServerSocket serverSocket;
    private ServerConfig serverConfig;
    private RelayThread relay;
    private int uuid;


    private UserRepository userRepository;
    private TrendRepository trendRepository;
    private UserLibrary userLibrary;
    private TrendLibrary trendLibrary;

    private OffLineMessageRepository offLineMessageRepository;
    private Map<String, TreeSet<OffLineMessage>> offlineMessages;

    private ServerController() {}

    /**
     * Retour l'instance du singleton du ServerController si non-null. Sinon, créer une nouvelle instance et la retourne également.
     *
     * @return L'instance du ServerController
     */
    public static ServerController getInstance() {
        if (instance == null) {
            instance = new ServerController();
        }
        return instance;
    }


    /**
     * Initialisation du ServerController
     *
     * @param userRepository Le dépôt d'utilisateurs enregistrés sur le server
     * @param trendRepository Le dépôt des tendances enregistrées sur le server
     */
    public void init(UserRepository userRepository, TrendRepository trendRepository, OffLineMessageRepository offLineMessageRepository) {
        try{
            this.serverConfig = new ServerJsonStorage().load();

            ServerSocketFactory socketFactory = serverConfig.tls ? SSLServerSocketFactory.getDefault() : ServerSocketFactory.getDefault();
            this.serverSocket = socketFactory.createServerSocket(serverConfig.serverPort);

            this.userRepository = userRepository;
            this.trendRepository = trendRepository;

            this.userLibrary = userRepository.load();
            this.trendLibrary = trendRepository.load();

            this.offLineMessageRepository = offLineMessageRepository;
            this.offlineMessages = offLineMessageRepository.load();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Lancement du serveur.
     * Initialisation des threads Executor et RelayThread et réception des clients.
     */
    public void start() throws IOException {
        welcome();
        try {
            this.relay = new RelayThread(this);
            TaskScheduler executor = Executor.getInstance();
            new Thread(executor).start();
            new Thread(this.relay).start();

            while (!this.serverSocket.isClosed()) {
                if (serverConfig.tls) {
                    SSLSocket client = (SSLSocket) serverSocket.accept();
                    ClientRunnable runnable = new ClientRunnable(client);
                    clientList.add(runnable);
                    new Thread(runnable).start();
                } else {
                    Socket client = serverSocket.accept();
                    ClientRunnable runnable = new ClientRunnable(client);
                    clientList.add(runnable);
                    new Thread(runnable).start();
                }
            }
        } catch (UnableToConnectToClientException | UnableToRunClientException | UnableToExecuteTaskException e) {
            e.printStackTrace();
        }
    }

    private void welcome() {
        UltraImportantClass.welcome();
        System.out.println("**************************************************************************************");
        System.out.println("********     SERVER ONLINE ! IP : " + serverConfig.serverDomain + " " + serverConfig.serverPort + "     ********");
        System.out.println("**************************************************************************************");
    }


    /*public boolean isRunning() {
        return !this.serverSocket.isClosed();
    }*/

    /**
     * Sauvegarde les utilisateurs et tendances.
     * Synchronized car plusieurs ClientRunnable peuvent appeler la méthode
     */
    public synchronized void save() throws UnableToSaveUserLibraryException, UnableToSaveTrendLibraryException {
        userRepository.save(this.userLibrary);
        trendRepository.save(this.trendLibrary);
    }

    /**
     * La signature "synchronized" signifie que la méthode est thread-safe et peut être accédée par plusieurs threads sans conflits.
     * @return String un id unique sous forme de chaine de caractères
     */
    public synchronized String generateId() {
        String generatedUniqueId = uuid+this.getServerConfig().serverDomain;
        uuid = (++uuid) % 10000;
        return generatedUniqueId;
    }


    @Override
    public void close() {
        try {
            this.relay.close();
            this.serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /************** GETTERS/SETTERS ***************/
    public synchronized UserLibrary getUserLibrary() {
        return userLibrary;
    }

    public TrendLibrary getTrendLibrary() {
        return trendLibrary;
    }

    /*public String getDomain() {
        try {
            return InetAddress.getLocalHost().getCanonicalHostName();
        } catch (UnknownHostException e) {
            return null;
        }
    }*/

    /**
     * Supprime un client de la liste des thread ClientRunnable
     *
     * @param client Le client à supprimer
     */
    public void removeClient(ClientRunnable client) {
        clientList.remove(client);
    }

    /**
     * Recupere un objet ClientRunnable en fonction du login (ex: antho123) passé en paramètre
     * @param login le login du client recherché
     * @return Un ClientRunnable si le client est bien trouvé dans la liste des clients connecté du server, null sinon
     */
    public ClientRunnable getClientRunnableByLogin(String login){
        for(ClientRunnable cr : clientList){
            if(cr.getUser().getLogin().equals(login)){
                return cr;
            }
        }
        return null;
    }

    public synchronized ServerConfig getServerConfig() {
        return serverConfig;
    }

    public RelayThread getRelay(){
        return this.relay;
    }



    public void addOfflineMessageForClient(UserCredentials userCredentials, OffLineMessage offLineMessage) {
        try {
            String client = userCredentials.toString();
            if(offlineMessages.containsKey(client)){
                offlineMessages.get(client).add(offLineMessage);
            }else{
                offlineMessages.put(client, new TreeSet<>());
                offlineMessages.get(client).add(offLineMessage);
            }

            offLineMessageRepository.save(offlineMessages);

        }catch (UnableToSaveOffLineMessageLibraryException e){
            System.out.println(e.getMessage());
        }
    }

    public synchronized boolean areOfflineMessagesForClient(ClientRunnable clientRunnable) {
        return offlineMessages.containsKey(clientRunnable.getUser().getCredentials().toString());
    }

    public synchronized TreeSet<OffLineMessage> getOfflineMessagesForClient(ClientRunnable clientRunnable) {
        return offlineMessages.get(clientRunnable.getUser().getCredentials().toString());
    }

    public void deleteOfflineMessagesForClient(ClientRunnable clientRunnable) {
        try{
            offlineMessages.remove(clientRunnable.getUser().getCredentials().toString());
            offLineMessageRepository.save(offlineMessages);
        }catch (UnableToSaveOffLineMessageLibraryException e){
            System.out.println(e.getMessage());
        }

    }
}