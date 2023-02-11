package org.helmo.murmurG6.system;

import java.util.concurrent.*;

public class Executor implements Runnable, AutoCloseable {

    private final ExecutorService executorService; //ExecutorService avec un seul thread pour exécuter les tâches de la file d'attente.
    private final BlockingQueue<Runnable> taskQueue; //File d'attente BlockingQueue appelée taskQueue pour stocker les tâches à exécuter.

    public Executor () {
        taskQueue = new LinkedBlockingQueue<>();
        executorService = Executors.newSingleThreadExecutor();
    }

    /**
     * Permet d'ajouter des tâches à la file d'attente
     *
     * @param task La tâche a ajouter à la file d'attente
     */
    public void addTask(Runnable task) {
        taskQueue.add(task);
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

    @Override
    public void run() {
        executorService.submit(() -> {
            while (true) {
                try {
                    Runnable task = taskQueue.take(); //Consomation des tâches de la file d'attente en appelant la méthode take de BlockingQueue, ce qui bloquera le thread jusqu'à ce qu'une tâche soit disponible dans la file d'attente.
                    task.run();
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
    }
}
