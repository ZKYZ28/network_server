package org.helmo.murmurG6.controller;

import org.helmo.murmurG6.models.BCrypt;
import org.helmo.murmurG6.models.Task;
import org.helmo.murmurG6.models.User;
import org.helmo.murmurG6.models.UserLibrary;
import org.helmo.murmurG6.models.exceptions.UserAlreadyRegisteredException;
import org.helmo.murmurG6.repository.exceptions.SaveUserCollectionException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.regex.Matcher;

public class Executor implements Runnable, AutoCloseable {

    private final ExecutorService executorService; //ExecutorService avec un seul thread pour exécuter les tâches de la file d'attente.
    private final BlockingQueue<Task> taskQueue; //File d'attente BlockingQueue appelée taskQueue pour stocker les tâches à exécuter.
    private final ServerController server;
    private final UserLibrary collection;

    public Executor (ServerController server) {
        this.taskQueue = new LinkedBlockingQueue<>();
        this.executorService = Executors.newSingleThreadExecutor();
        this.server = server;
        this.collection = server.getUserCollection();
    }

    /**
     * Permet d'ajouter des tâches à la file d'attente
     *
     * @param task La tâche a ajouter à la file d'attente
     */
    public void addTask(Task task) {
        taskQueue.add(task);
    }

    /**
     * Fait tourner l'executor sur un thread
     */
    @Override
    public void run() {
        executorService.submit(() -> {
            while (true) {
                try {
                    //L'executor bloque ici jusqu'à ce qu'une nouvelle tache arrvie
                    Task task = taskQueue.take(); //Consomation des tâches de la file d'attente en appelant la méthode take de BlockingQueue, ce qui bloquera le thread jusqu'à ce qu'une tâche soit disponible dans la file d'attente.
                    executeTask(task);
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
    }


    /**
     * Execute la tache passée en paramètre selon son type
     * @param task la tache à exécuter par l'éxécutor
     */
    private void executeTask(Task task) {

        ClientRunnable client = task.getClient(); //On récupère le client à qui on fait la tache
        Matcher params = task.getMatcher();   //On récupère le matcher de la tache à éxécuter
        User user;

        switch (task.getType()) {
            case REGISTER:
                user = new User(params.group(1), BCrypt.of(params.group(4)), new ArrayList<String>(), new ArrayList<String>());
                client.sendMessage(register(user, client));
                break;

            case CONNECT:
                user = collection.get(params.group(1));
                client.setUser(user);
                String login = params.group(1);
                client.sendMessage(connect(login));
                break;

            case CONFIRM:
                user = client.getUser();
                String received = params.group(1);
                String expected = user.getBcrypt().generateChallenge(client.getRandom22());
                client.sendMessage(confirm(received, expected));
                break;

            case MSG:
                server.broadcastToAllClientsExceptMe(client, params.group(1));
                break;
        }
    }

    /**
     * Retourne le message à envoyer au client lorsque celui veut se connecter
     * @param login le loggin de l'utilisateur qui veut se connecter
     * @return le message PARAM oubien -ERR
     */
    private String connect(String login) {
        if (collection.containsKey(login)) {
            User user = collection.get(login);
            return "PARAM " + user.getBcryptRound() + " " + user.getBcryptSalt();
        } else {
            return "-ERR";
        }
    }


    /**
     * Retourne la confirmation ou non de la connexion du client
     * @param clientChallenge le challenge calculé par le client
     * @param userChallenge le challenge calculé par le server
     * @return le message "+OK" si le challenge-client correspond au challenge-server. "-ERR" sinon
     */
    private String confirm(String clientChallenge, String userChallenge){
        return clientChallenge.equals(userChallenge) ? "+OK" : "-ERR";
    }

    private String register(User user, ClientRunnable client)  {
        try {
            server.registerUser(user);
            client.setUser(user);
            return "+OK";
        } catch (SaveUserCollectionException | UserAlreadyRegisteredException e) {
            return "-ERR";
        }
    }


    @Override
    public void close() {
        this.executorService.shutdown();
        try {
            if (!this.executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                this.executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            this.executorService.shutdownNow();
        }
    }
}
