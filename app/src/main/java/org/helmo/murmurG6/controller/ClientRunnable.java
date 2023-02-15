package org.helmo.murmurG6.controller;

import org.helmo.murmurG6.models.Task;
import org.helmo.murmurG6.models.Protocol;
import org.helmo.murmurG6.models.User;

import javax.net.ssl.SSLSocket;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ClientRunnable implements Runnable {
    private BufferedReader in;
    private PrintWriter out;
    private User user;
    private String random22;


    public ClientRunnable(Socket client) {
        try {
            in = new BufferedReader(new InputStreamReader(client.getInputStream(), StandardCharsets.UTF_8));
            out = new PrintWriter(new OutputStreamWriter(client.getOutputStream(), StandardCharsets.UTF_8), true);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void run() {
        try {
            TaskScheduler executor = Executor.getInstance();
            random22 = executor.sayHello(this);                    //Envoi du message Hello au client + récupération du random de 22 caractères aléatoires
            String ligne = in.readLine();                               //Le server attend que le client ecrive quelque chose

            while (ligne != null && !ligne.isEmpty()) {
                System.out.printf("Ligne reçue : %s\r\n", ligne);

                Task task = Protocol.buildTask(ligne); //Création d'une tache sur base de la ligne recue
                task.setClient(this);      //Asignation du ClientRunnable à la tache (utile pour l'executor)
                executor.addTask(task);     //Ajout de la tache dans la file de taches de l'executor

                ligne = in.readLine();    //Le thread mis à disposition du client attend la prochaine ligne
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void sendMessage(String message) {
        out.println(message);
        out.flush();
    }

    public User getUser() {
        return this.user;
    }

    public void setUser(User u) {
        this.user = u;
    }

    public String getRandom22() {
        return random22;
    }
}
