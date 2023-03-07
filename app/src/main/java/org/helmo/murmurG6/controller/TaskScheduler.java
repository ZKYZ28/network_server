package org.helmo.murmurG6.controller;

import org.helmo.murmurG6.models.Task;

public interface TaskScheduler extends Runnable, AutoCloseable {

    void addTask(Task task);

}
