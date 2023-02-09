package org.helmo.murmurG6.server;

import org.helmo.murmurG6.client.ClientRunnable;
import org.helmo.murmurG6.models.UserCollection;
import org.helmo.murmurG6.repository.UserCollectionRepository;

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

    private final UserCollection userCollection;

    public ServerController(int port, UserCollectionRepository ucr) throws IOException {
        clientList = Collections.synchronizedList(new ArrayList<>());
        this.serverSocket = new ServerSocket(port);
        this.userCollection = ucr.read();
        System.out.println("SERVER ONLINE ! IP : " + getIp());

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

    public String getIp() {
        try {
            this.serverSocket.getInetAddress();
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    @Override
    public void close() throws IOException {
        try {
            this.serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}