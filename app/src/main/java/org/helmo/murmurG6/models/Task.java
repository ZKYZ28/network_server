package org.helmo.murmurG6.models;

import org.helmo.murmurG6.system.ClientRunnable;

import java.util.regex.Matcher;

public class Task {
    private ClientRunnable client;
    private final TaskType type;
    private final Matcher matcher;
    private final String taskParams;

    public Task(TaskType type, Matcher matcher, String taskParams){
        this.type = type;
        this.matcher = matcher;
        this.taskParams = taskParams;
    }

    public void setClient(ClientRunnable client){
        this.client = client;
    }
    public ClientRunnable getClient(){
        return this.client;
    }
    public TaskType getType() {
        return type;
    }

    public Matcher getMatcher() {
        return matcher;
    }

    public String getTaskParams() {
        return taskParams;
    }

}
