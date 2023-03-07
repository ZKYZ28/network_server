package org.helmo.murmurG6.controller;

import org.helmo.murmurG6.controller.exceptions.UnableToConnectToClientException;
import org.helmo.murmurG6.controller.exceptions.UnableToExecuteTaskException;
import org.helmo.murmurG6.controller.exceptions.UnableToRunClientException;
import org.helmo.murmurG6.executor.Executor;
import org.helmo.murmurG6.infrastructure.ServerJsonStorage;
import org.helmo.murmurG6.models.*;
import org.helmo.murmurG6.repository.TrendRepository;
import org.helmo.murmurG6.repository.UserRepository;
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

/**
 * La classe ServerController représente le contrôleur principal de l'application serveur.
 * Cette classe gère la connexion des clients et communique avec la classe ClientRunnable pour gérer la communication avec les clients.
 */
public class ServerController implements AutoCloseable {

    private static ServerController instance;
    private final Set<ClientRunnable> clientList = Collections.synchronizedSet(new HashSet<>());
    private SSLServerSocket serverSocket;
    private ServerConfig serverConfig;
    private RelayThread relay;
    private int uuid;


    private UserRepository userRepository;
    private TrendRepository trendRepository;
    private UserLibrary userLibrary;
    private TrendLibrary trendLibrary;


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
     * @param port Le port sur lequel les clients se connectent
     * @param userRepository Le dépôt d'utilisateurs enregistrés sur le server
     * @param trendRepository Le dépôt des tendances enregistrées sur le server
     */
    public void init(int port, UserRepository userRepository, TrendRepository trendRepository) {
        try{
            SSLServerSocketFactory sslServerSocketFactory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
            this.serverSocket = (SSLServerSocket) sslServerSocketFactory.createServerSocket(port);

            this.serverConfig = new ServerJsonStorage().load();
            //this.serverConfig.setServerIp(getDomain());

            this.userRepository = userRepository;
            this.trendRepository = trendRepository;

            this.userLibrary = userRepository.load();
            this.trendLibrary = trendRepository.load();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Lancement du serveur.
     * Initialisation des threads Executor et RelayThread et réception des clients.
     *
     * @throws IOException
     */
    public void start() throws IOException {
        welcome();
        try {
            this.relay = new RelayThread(this);
            TaskScheduler executor = Executor.getInstance();
            new Thread(executor).start();
            new Thread(this.relay).start();

            while (!this.serverSocket.isClosed()) {
                SSLSocket client = (SSLSocket) serverSocket.accept();
                ClientRunnable runnable = new ClientRunnable(client);
                clientList.add(runnable);
                new Thread(runnable).start();
            }
        } catch (UnableToConnectToClientException | UnableToRunClientException | UnableToExecuteTaskException e) {
            e.printStackTrace();
        }
    }

    private void welcome() {
        UltraImportantClass.welcome();
        System.out.println("**************************************************************************************");
        System.out.println("********     SERVER ONLINE ! IP : " + serverConfig.getServerName() + " " + serverSocket.getInetAddress().getHostAddress() + "     ********");
        System.out.println("**************************************************************************************");
    }


    /*public boolean isRunning() {
        return !this.serverSocket.isClosed();
    }*/

    /**
     * Sauvegarde les utilisateurs et tendances.
     * Synchronized car plusieurs ClientRunnable peuvent appeler la méthode
     *
     * @throws UnableToSaveUserLibraryException
     * @throws UnableToSaveTrendLibraryException
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
        String generatedUniqueId = uuid+this.getServerConfig().getServerName();
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
}