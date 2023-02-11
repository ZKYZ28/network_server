package org.helmo.murmurG6.system;

import org.helmo.murmurG6.models.Task;
import org.helmo.murmurG6.models.Protocol;
import org.helmo.murmurG6.models.TaskType;
import org.helmo.murmurG6.models.User;
import org.helmo.murmurG6.utils.RandomSaltGenerator;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;

public class ClientRunnable implements Runnable {
    private BufferedReader in;
    private PrintWriter out;
    private boolean isConnected = false;
    private final ServerController server;
    private User user;
    private Executor executor;

    private final Protocol protocol = new Protocol();

    public ClientRunnable(Socket client, ServerController server) {
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
            String r22 = sayHello();    //Reconnaissance du murmur.client.server par le murmur.client (ou sinon crash)
            String login ="";
            String ligne = in.readLine();      //Le murmur.client.server attend que le murmur.client ecrive quelque chose
            while(isConnected && ligne != null && !ligne.isEmpty()) {   //Quand le murmur.client envoie sa ligne
                System.out.printf("Ligne reçue : %s\r\n", ligne);    //Le murmur.client.server recoit la ligne

                Task task = protocol.analyseMessage(ligne);
                task.setClient(this);
                executor.addTask(task);

                ligne = in.readLine();    //Le thread mis à disposition du murmur.client attend la prochaine ligne
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

    private String sayHello() {
        String salt = RandomSaltGenerator.generateSalt();
        sendMessage("HELLO " + server.getIp() + " " + salt);
        return salt;
    }

    public User getUser(){
        return this.user;
    }

    public void setUser(User u) {
        this.user = u;
    }
}
