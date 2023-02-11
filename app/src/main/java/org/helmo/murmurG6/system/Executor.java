package org.helmo.murmurG6.system;

import org.helmo.murmurG6.models.BcryptHash;
import org.helmo.murmurG6.models.Task;
import org.helmo.murmurG6.models.User;

import java.util.concurrent.*;
import java.util.regex.Matcher;

public class Executor implements Runnable, AutoCloseable {

    private final ExecutorService executorService; //ExecutorService avec un seul thread pour exécuter les tâches de la file d'attente.
    private final BlockingQueue<Task> taskQueue; //File d'attente BlockingQueue appelée taskQueue pour stocker les tâches à exécuter.

    private final ServerController server;

    public Executor (ServerController server) {
        taskQueue = new LinkedBlockingQueue<>();
        executorService = Executors.newSingleThreadExecutor();
        this.server = server;
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

        switch (task.getType()){
            case REGISTER:
                try{
                    User u = new User(params.group(1), BcryptHash.decomposeHash(params.group(4)));
                    server.registerUser(u);
                    client.setUser(u);
                }catch (RegistrationImpossibleException e){
                    System.out.println(e.getMessage());
                }
                break;

            case CONNECT:
                client.sendMessage(connect(params.group(1), client.getUser().getBcryptSalt()));
                break;

            case CONFIRM:
                client.sendMessage(confirm(params.group(1), client.getUser().getBcrypt().calculateChallenge(client.getRandom22())));
                break;
        }
    }

    private String connect(String loggin, String salt){
        if (server.getUserCollection().isRegistered(loggin)) {
            System.out.println("j'envoie param");
            return "PARAM " + server.getUserCollection().getRegisteredUsers().get(loggin).getBcryptRound() + " " +salt;
        } else {
            return  "-ERR";
        }
    }

    private String confirm(String challenge, String userBcrypt){
        return challenge.equals(userBcrypt) ? "+OK" : "-ERR";
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
