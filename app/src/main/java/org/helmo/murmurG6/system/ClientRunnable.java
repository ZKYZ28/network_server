package org.helmo.murmurG6.system;

import org.helmo.murmurG6.models.Task;
import org.helmo.murmurG6.models.Protocol;
import org.helmo.murmurG6.models.User;
import org.helmo.murmurG6.utils.RandomSaltGenerator;

import javax.net.ssl.SSLSocket;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ClientRunnable implements Runnable {
    private BufferedReader in;
    private PrintWriter out;
    private boolean isConnected = false;
    private final ServerController server;
    private User user;
    private Executor executor;

    private String random22 = "";

    private final Protocol protocol = new Protocol();

    public ClientRunnable(SSLSocket client, ServerController server) {
        this.server =  server;
        this.executor = server.getExecutor();
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
            random22 = sayHello();                                      //Envoi du message Hello au client + récupération du random de 22 caractères aléatoires
            String login ="";                                           //Le loggin du client
            String ligne = in.readLine();                               //Le server attend que le client ecrive quelque chose
            while(isConnected && ligne != null && !ligne.isEmpty()) {
                System.out.printf("Ligne reçue : %s\r\n", ligne);

                Task task = protocol.analyseMessage(ligne); //Création d'une tache sur base de la ligne recue
                task.setClient(this);      //Asignation du ClientRunnable à la tache (utile pour l'executor)
                executor.addTask(task);     //Ajout de la tache dans la file de taches de l'executor

                ligne = in.readLine();    //Le thread mis à disposition du client attend la prochaine ligne
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
        if (isConnected) {
            out.println(message);
            out.flush();
        }
    }

    /**
     * Envoi le message "Hello" + une chaine de 22 caractères aléatoire
     * @return la chaine de caractère aléatoire
     */
    private String sayHello() {
        String random22 = RandomSaltGenerator.generateSalt();
        sendMessage("HELLO " + server.getIp() + " " + random22);
        return random22;
    }

    public User getUser(){
        return this.user;
    }

    public void setUser(User u) {
        this.user = u;
    }

    public String getRandom22(){
        return random22;
    }
}
