package org.helmo.murmurG6.system;

import org.helmo.murmurG6.models.BCrypt;
import org.helmo.murmurG6.models.Task;
import org.helmo.murmurG6.models.User;
import org.helmo.murmurG6.models.UserCollection;
import java.util.concurrent.*;
import java.util.regex.Matcher;

public class Executor implements Runnable, AutoCloseable {

    private final ExecutorService executorService; //ExecutorService avec un seul thread pour exécuter les tâches de la file d'attente.
    private final BlockingQueue<Task> taskQueue; //File d'attente BlockingQueue appelée taskQueue pour stocker les tâches à exécuter.
    private final ServerController server;
    private final UserCollection collection;

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

    @Override
    public void run() {
        executorService.submit(() -> {
            while (true) {
                try {
                    Task task = taskQueue.take(); //Consomation des tâches de la file d'attente en appelant la méthode take de BlockingQueue, ce qui bloquera le thread jusqu'à ce qu'une tâche soit disponible dans la file d'attente.
                    executeTask(task);
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
    }

    public void executeTask(Task task) {

        ClientRunnable client = task.getClient();
        Matcher params = task.getMatcher();
        User user;

        switch (task.getType()) {
            case REGISTER:
                user = new User(params.group(1), BCrypt.decomposeHash(params.group(4)));
                client.sendMessage(register(user, client));
                break;

            case CONNECT:
                user = collection.getUserFromLogin(params.group(1));
                client.setUser(user);
                String login = params.group(1);
                client.sendMessage(connect(login));
                break;

            case CONFIRM:
                user = client.getUser();
                String received = params.group(1);
                String expected = user.getBcrypt().calculateChallenge(client.getRandom22());
                client.sendMessage(confirm(received, expected));
                break;
        }
    }

    private String connect(String login){
        if (collection.isRegistered(login)) {
            User user = collection.getUserFromLogin(login);
            return "PARAM " + user.getBcryptRound() + " " + user.getBcryptSalt();
        } else {
            return "-ERR";
        }
    }

    private String confirm(String challenge, String userBcrypt){
        return challenge.equals(userBcrypt) ? "+OK" : "-ERR";
    }

    private String register(User user, ClientRunnable client)  {
        try {
            server.registerUser(user);
            client.setUser(user);
            return "+OK";
        } catch (RegistrationImpossibleException e) {
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
