package org.helmo.murmurG6.controller;

import org.helmo.murmurG6.models.BCrypt;
import org.helmo.murmurG6.models.Task;
import org.helmo.murmurG6.models.User;
import org.helmo.murmurG6.models.UserLibrary;
import org.helmo.murmurG6.models.exceptions.UserAlreadyRegisteredException;
import org.helmo.murmurG6.repository.exceptions.SaveUserCollectionException;
import org.helmo.murmurG6.utils.RandomSaltGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.regex.Matcher;

public class Executor implements TaskScheduler {

    private static Executor instance;
    private final ExecutorService executorService; //ExecutorService avec un seul thread pour exécuter les tâches de la file d'attente.
    private final BlockingQueue<Task> taskQueue; //File d'attente BlockingQueue appelée taskQueue pour stocker les tâches à exécuter.
    private ServerController server;

    private Executor() {
        this.taskQueue = new LinkedBlockingQueue<>();
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public static Executor getInstance() {
        if (instance == null) {
            instance = new Executor();
        }
        return instance;
    }

    public void setServer(ServerController server) {
        this.server = server;
    }

    /**
     * Permet d'ajouter des tâches à la file d'attente
     *
     * @param task La tâche a ajouter à la file d'attente
     */
    public void addTask(Task task) {
        this.taskQueue.add(task);
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

    private void executeTask(Task task) {

        ClientRunnable client = task.getClient(); //On récupère le client à qui on fait la tache
        Matcher params = task.getMatcher();   //On récupère le matcher de la tache à éxécuter
        User user;

        switch (task.getType()) {
            case REGISTER -> {
                user = new User(params.group("username"), BCrypt.of(params.group("bcrypt")), new ArrayList<>(), new ArrayList<>());
                client.sendMessage(register(user, client));
            }
            case CONNECT -> {
                user = server.getUserCollection().get(params.group("username"));
                client.setUser(user);
                client.sendMessage(connect(user.getLogin()));
            }
            case CONFIRM -> {
                user = client.getUser();
                String received = params.group("challenge");
                String expected = user.getBcrypt().generateChallenge(client.getRandom22());
                client.sendMessage(confirm(received, expected));
            }
            case MSG -> server.broadcastToAllClientsExceptMe(client, params.group("message"));
        }
    }


    private String connect(String login) {
        if (server.getUserCollection().containsKey(login)) {
            User user = server.getUserCollection().get(login);
            return "PARAM " + user.getBcryptRound() + " " + user.getBcryptSalt();
        } else {
            return "-ERR";
        }
    }


    private String confirm(String clientChallenge, String userChallenge) {
        return clientChallenge.equals(userChallenge) ? "+OK" : "-ERR";
    }

    private String register(User user, ClientRunnable client) {
        try {
            server.registerUser(user);
            client.setUser(user);
            return "+OK";
        } catch (SaveUserCollectionException | UserAlreadyRegisteredException e) {
            return "-ERR";
        }
    }


    public String sayHello(ClientRunnable client) {
        String random22 = RandomSaltGenerator.generateSalt();
        client.sendMessage("HELLO " + server.getIp() + " " + random22);
        return random22;
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
