package org.helmo.murmurG6.controller;

import org.helmo.murmurG6.models.Task;

public interface TaskScheduler {

    /**
     * Ajoute une tache dans la file de taches de l'executor
     * @param task
     */
    void addTask(Task task);



}
