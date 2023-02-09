package org.helmo.murmurG6.client;

import org.helmo.murmurG6.models.Message;
import org.helmo.murmurG6.models.Protocol;
import org.helmo.murmurG6.server.ServerController;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ClientRunnable implements Runnable {
    private final Socket monClient;
    private BufferedReader in;
    private PrintWriter out;
    private boolean isConnected = false;
    private final ServerController controller;

    public ClientRunnable(Socket client, ServerController controller) {
        this.monClient = client;
        this.controller=  controller;
        try {
            in = new BufferedReader(new InputStreamReader(client.getInputStream(), StandardCharsets.UTF_8));
            out = new PrintWriter(new OutputStreamWriter(client.getOutputStream(), StandardCharsets.UTF_8), true);
            isConnected = true;
        } catch(IOException ex) {
            ex.printStackTrace();
        }
    }

    public void run() {
        try {
            sendMessage("HELLO 192.168.148.47 azertyuiopmlkjhgfdsqwx"); //Reconnaissance du murmur.client.server par le murmur.client (ou sinon crash)
            String ligne = in.readLine(); //Le murmur.client.server attend que le murmur.client ecrive quelque chose
            while(isConnected && ligne != null) { //Quand le murmur.client envoie sa ligne
                System.out.printf("Ligne reçue : %s\r\n", ligne); //Le murmur.client.server recoit la ligne
                controller.broadcastToAllClientsExceptMe(this, ligne); //Il la publie à tous les clients dans la file
                ligne = in.readLine(); //Le thread mis à disposition du murmur.client attend la prochaine ligne
            }
        } catch(IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * La méthode sendMessage permet d'envoyer un message au murmur.client associé à cet objet ClientRunnable.
     *
     * @param message Le message à envoyer au murmur.client.
     */
    public void sendMessage(String message) {
        if(isConnected) {
            out.println(message);
            out.flush();
        }
    }
}
