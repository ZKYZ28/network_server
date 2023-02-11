package org.helmo.murmurG6.system;

import org.helmo.murmurG6.models.User;
import org.helmo.murmurG6.models.UserCollection;
import org.helmo.murmurG6.models.exceptions.UserAlreadyRegisteredException;
import org.helmo.murmurG6.repository.IUserCollectionRepository;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ServerController implements AutoCloseable {
    private final List<ClientRunnable> clientList;
    private final ServerSocket serverSocket;
    private final IUserCollectionRepository repo;
    private final UserCollection userCollection;
    private final Executor executor;

    public ServerController(int port, IUserCollectionRepository repo) throws IOException {
        this.executor = new Executor();
        this.clientList = Collections.synchronizedList(new ArrayList<>());
        this.serverSocket = new ServerSocket(port);
        this.repo = repo;
        this.userCollection = repo.read(); //remplissage de tous les users inscrits dans la usercollection
        System.out.println("SERVER ONLINE ! IP : " + getIp());
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
        } catch (UserAlreadyRegisteredException e) {
            //*************************************
                e.printStackTrace();
                //Mieux: sendMessageToUser => e.message
            //**************************************
        }
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

    @Override
    public void close() {
        try {
            this.serverSocket.close();
            this.repo.save(userCollection); //On sauvegarde le contenu de la userCollection à la fermeture du server
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}