package org.helmo.murmurG6.executor;

import org.helmo.murmurG6.models.Protocol;
import org.helmo.murmurG6.controller.ServerController;
import org.helmo.murmurG6.controller.TaskScheduler;
import org.helmo.murmurG6.controller.exceptions.UnableToExecuteTaskException;
import org.helmo.murmurG6.models.*;

import java.util.concurrent.*;
import java.util.regex.Matcher;

public class Executor implements TaskScheduler {

    private static Executor instance;
    private final ExecutorService executorService; //ExecutorService avec un seul thread pour exécuter les tâches de la file d'attente.
    private final BlockingQueue<Task> taskQueue;   //File d'attente BlockingQueue appelée taskQueue pour stocker les tâches à exécuter.
    private final ServerController server = ServerController.getInstance();


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


    /**
     * Ajoute une tâche a la liste bloquante de tâches
     *
     * @param task La tâche à ajouter
     */
    public synchronized void addTask(Task task) {
        taskQueue.add(task);
    }

    @Override
    public void run() {
        executorService.submit(() -> {
            while (!this.executorService.isTerminated()) {
                try {
                    Task task = taskQueue.take(); //Consomation des tâches de la file d'attente en appelant la méthode take de BlockingQueue, ce qui bloquera le thread (waiting) jusqu'à ce qu'une tâche soit disponible dans la file d'attente.
                    executeTask(task);
                } catch (InterruptedException e) {
                    throw new UnableToExecuteTaskException("Une erreur est survenue lors de l'exécution de la tâche", e);
                }
            }
        });
    }



    private void executeTask(Task task) {
        Matcher params = Protocol.getMatcher(task.getType(), task.getContent());

        if(params != null && params.matches()) {
                switch (task.getType()) {

                case MSG:
                    System.out.println("MSG en cours de traitement");
                    MSGExecutor.castMsg(server.getClientRunnableByLogin(task.getSender().getLogin()), params.group("message"), task.getTaskId());
                    break;

                case MSGS:
                    System.out.println("MSGS en cours de traitement");
                    MSGSExecutor.castMsgs(task, params.group("message"));
                    break;

                case FOLLOW:
                    System.out.println("FOLLOW en cours de traitement");
                    FollowExecutor.follow(task, params.group("domain"));
                    break;

                default:
                    System.out.println("Erreur de traitement");
                    break;
            }
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


    /********** GETTERS/SETTERS ************/


    public void sendToRelay(String sendMessage) {
        this.server.getRelay().sendToRelay(sendMessage);
    }
}