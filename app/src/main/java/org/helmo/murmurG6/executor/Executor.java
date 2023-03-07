package org.helmo.murmurG6.executor;

import org.helmo.murmurG6.controller.ClientRunnable;
import org.helmo.murmurG6.controller.RelayThread;
import org.helmo.murmurG6.models.Protocol;
import org.helmo.murmurG6.controller.ServerController;
import org.helmo.murmurG6.controller.TaskScheduler;
import org.helmo.murmurG6.controller.exceptions.UnableToExecuteTaskException;
import org.helmo.murmurG6.models.*;
import org.helmo.murmurG6.utils.RandomSaltGenerator;
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

    public void addTask(Task task) {
        taskQueue.add(task);
    }

    @Override
    public void run() {
        executorService.submit(() -> {
            while (server.isRunning()) {
                try {
                    Task task = taskQueue.take(); //Consomation des tâches de la file d'attente en appelant la méthode take de BlockingQueue, ce qui bloquera le thread (waiting) jusqu'à ce qu'une tâche soit disponible dans la file d'attente.
                    executeTask(task);
                } catch (InterruptedException e) {
                    throw new UnableToExecuteTaskException("Une erreur est survenue lors de l'exécution de la tâche", e);
                }
            }
        });
    }


    //TODO refactor task -> client runnable
    //TODO Enlever statique
    //TODO Class Statique -> private constructor + class final
    //TODO Relay Thread -> retirer singleton
    private void executeTask(Task task) {
        ClientRunnable client = task.getClient();
        Matcher params = Protocol.getMatcher(task.getType(), task.getContent());

        if(params != null && client != null) {
            switch (task.getType()) {
                case REGISTER:
                    RegisterExecutor.register(client, params);
                    break;

                case CONNECT:
                    ConnectExecutor.connect(client, params);
                    break;

                case CONFIRM:
                    ConfirmExecutor.confirm(client, params.group("challenge"));
                    break;

                case MSG:
                    MSGExecutor.castMsg(client, params.group("message"), task.getTaskId());
                    break;

                case MSGS:
                    MSGSExecutor.castMsgs(task.getSender(), task.getReceiver(), params.group("message"), task.getTaskId());
                    break;

                case FOLLOW:
                    FollowExecutor.follow(client.getUser().getCredentials(), params.group("domain"));
                    break;

                case DISCONNECT:
                    server.removeClient(client);
                    break;

                default:
                    client.sendMessage(Protocol.build_ERROR());
                    break;
            }
        }
    }


    public String sayHello(ClientRunnable client) {
        String random22 = RandomSaltGenerator.generateSalt();
        client.sendMessage(Protocol.build_HELLO(server.getServerConfig().getServerName(), random22));
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


    /********** GETTERS/SETTERS ************/

    public static Executor getInstance() {
        if (instance == null) {
            instance = new Executor();
        }
        return instance;
    }


    public void sendToRelay(String sendMessage) {
        //RelayThread.getInstance().sendToRelay(sendMessage);
    }
}