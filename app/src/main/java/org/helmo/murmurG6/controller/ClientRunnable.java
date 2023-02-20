package org.helmo.murmurG6.controller;

import org.helmo.murmurG6.controller.exceptions.UnableToConnectToClientException;
import org.helmo.murmurG6.controller.exceptions.UnableToRunClientException;
import org.helmo.murmurG6.executor.Executor;
import org.helmo.murmurG6.models.Protocol;
import org.helmo.murmurG6.models.Task;
import org.helmo.murmurG6.models.User;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * La classe ClientRunnable est utilisée pour gérer la communication avec les clients connectés au serveur.
 * Elle implémente l'interface Runnable et est exécutée dans un thread séparé pour chaque client connecté.
 */
public class ClientRunnable implements Runnable {
    private final BufferedReader in;
    private final PrintWriter out;
    private User user;
    private String random22;


    /**
     * Constructeur de la classe ClientRunnable.
     * Établit une connexion avec le client passé en paramètre et crée un BufferedReader et un PrintWriter pour la communication avec le client.
     *
     * @param client Socket du client connecté
     * @throws UnableToConnectToClientException si la connexion au client échoue
     */
    public ClientRunnable(Socket client) throws UnableToConnectToClientException {
        try {
            in = new BufferedReader(new InputStreamReader(client.getInputStream(), StandardCharsets.UTF_8));
            out = new PrintWriter(new OutputStreamWriter(client.getOutputStream(), StandardCharsets.UTF_8), true);
        } catch (IOException ex) {
            throw new UnableToConnectToClientException("Impossible de se connecter au client. Une erreur est survenue lors de la création des canaux de communication pour le thread client. Vérifiez que le client est correctement connecté et que les ports de communication sont disponibles.", ex);
        }
    }

    /**
     * Méthode run de l'interface Runnable.
     * Cette méthode est exécutée dans un thread séparé pour chaque client connecté.
     * Elle lit les messages envoyés par le client, crée des tâches en fonction des messages reçus, et les ajoute à la file d'attente du TaskScheduler (Executor).
     * Si une exception est levée, la méthode lève une UnableToRunClientException.
     *
     * @throws UnableToRunClientException si une erreur se produit lors de la communication avec le client
     */
    public void run() throws UnableToRunClientException {
        try {
            //L'executor est un singleton. Le thread connait l'executor via une interface
            TaskScheduler executor = Executor.getInstance();

            //Envoi du message Hello au client + récupération du random de 22 caractères aléatoires
            random22 = executor.sayHello(this);



            String ligne = in.readLine();

            while (ligne != null && !ligne.isEmpty()) {
                System.out.printf("Ligne reçue : %s\r\n", ligne);

                //Ajout de la tache dans la file des taches de l'executor
                executor.addTask(new Task(user != null ? user.getCredentials() : null, this, Protocol.detectTaskType(ligne), ligne));

                ligne = in.readLine();
            }
        } catch (IOException ex) {
            throw new UnableToRunClientException("Une erreur est survenue lors de la lecture du socket.", ex);
        }
    }

    /**
     * Envoie un message au client.
     *
     * @param message Le message à envoyer.
     */
    public void sendMessage(String message) {
        out.println(message);
        out.flush();
    }


    /***************** GETTERS/SETTERS *****************/


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
