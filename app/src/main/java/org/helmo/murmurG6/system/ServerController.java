package org.helmo.murmurG6.system;

import org.helmo.murmurG6.models.User;
import org.helmo.murmurG6.models.UserCollection;
import org.helmo.murmurG6.models.exceptions.UserAlreadyRegisteredException;
import org.helmo.murmurG6.repository.IUserCollectionRepository;
import org.helmo.murmurG6.repository.exceptions.SaveUserCollectionException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ServerController implements AutoCloseable {
    private final List<ClientRunnable> clientList = Collections.synchronizedList(new ArrayList<>());
    private final ServerSocket serverSocket;
    private final IUserCollectionRepository repo;
    private final Executor executor = new Executor();
    private final UserCollection userCollection = new UserCollection();

    public ServerController(int port, IUserCollectionRepository repo) throws IOException {
        this.serverSocket = new ServerSocket(port);
        this.repo = repo;
        this.userCollection.setRegisteredUsers(repo.read()); //remplissage de tous les users inscrits dans la usercollection
        System.out.println("****************************************************************");
        System.out.println("********     SERVER ONLINE ! IP : " + getIp()+"        **********");
        System.out.println("****************************************************************");
    }

    public void start() throws IOException {
        this.executor.run();
        while(true) {
            Socket client = serverSocket.accept();
            System.out.println("Quelqu'un s'est connecté!");
            ClientRunnable runnable = new ClientRunnable(client, this);
            clientList.add(runnable);
            (new Thread(runnable)).start();
        }
    }

    public void broadcastToAllClientsExceptMe(ClientRunnable me, String message) {
        System.out.printf("[broadcastAll] Message envoyé : %s\n", message);
        for(ClientRunnable c : clientList) {
            if (c != me) {
                c.sendMessage(message);
            }
        }
    }

    public void registerUser(User user) {
        try{
            userCollection.registerUser(user);
            repo.save(userCollection.getRegisteredUsers().values()); //On sauvegarde le contenu de la userCollection à la fermeture du server
        } catch (UserAlreadyRegisteredException | SaveUserCollectionException e) {
            //*************************************
                e.printStackTrace();
                //Mieux: sendMessageToUser => e.message
            //**************************************
        }
    }


    public UserCollection getUserCollection() {
        return userCollection;
    }

    public String getIp() {
        try {
            this.serverSocket.getInetAddress();
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public Executor getExecutor() {
        return executor;
    }


    /*
    Le close sert il a quelque chose ici ? car le server tourne à l'infini et se termine à la fin du programme
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