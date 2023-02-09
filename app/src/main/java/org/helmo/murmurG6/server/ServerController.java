package org.helmo.murmurG6.server;

import org.helmo.murmurG6.infrastructure.client.ClientRunnable;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ServerController {
    private final List<ClientRunnable> clientList;

    public ServerController(int port) {
        clientList = Collections.synchronizedList(new ArrayList<>());
        Socket client;
        try (ServerSocket server = new ServerSocket(port)) {
            System.out.println("Server online");
            while(true) {
                client = server.accept();
                System.out.println("Quelqu'un s'est connecté!");
                ClientRunnable runnable = new ClientRunnable(client, this);
                clientList.add(runnable);
                (new Thread(runnable)).start();
            }
        } catch(IOException ex) {
            ex.printStackTrace();
        }
    }

    public void broadcastToAllClientsExceptMe(ClientRunnable me, String message) {
        System.out.printf("[broadcastAll] Message envoyé : %s\n", message);
        for(ClientRunnable c : clientList) {
            if(c != me)
                c.sendMessage(message);
        }
    }


}

