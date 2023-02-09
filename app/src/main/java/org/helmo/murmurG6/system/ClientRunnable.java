package org.helmo.murmurG6.system;

import org.helmo.murmurG6.system.ServerController;
import org.helmo.murmurG6.utils.RandomSaltGenerator;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ClientRunnable implements Runnable {
    private BufferedReader in;
    private PrintWriter out;
    private boolean isConnected = false;
    private final ServerController controller;

    public ClientRunnable(Socket client, ServerController controller) {
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
            sayHello();                                                 //Reconnaissance du murmur.client.server par le murmur.client (ou sinon crash)
            String ligne = in.readLine();                               //Le murmur.client.server attend que le murmur.client ecrive quelque chose
            while(isConnected && ligne != null && !ligne.isEmpty()) {   //Quand le murmur.client envoie sa ligne
                System.out.printf("Ligne reçue : %s\r\n", ligne);       //Le murmur.client.server recoit la ligne

                //Réagir à la ligne recue (Executor){

                    //si ligne === REGISTER -> envoyer +OK sinon -ERR
                //}

                controller.broadcastToAllClientsExceptMe(this, ligne); //Il la publie à tous les clients dans la file
                ligne = in.readLine();                                     //Le thread mis à disposition du murmur.client attend la prochaine ligne
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

    private void sayHello() {
        sendMessage("HELLO " + controller.getIp() + " " + RandomSaltGenerator.generateSalt());
    }
}
