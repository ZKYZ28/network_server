package org.helmo.murmurG6.models;

import org.helmo.murmurG6.controller.ClientRunnable;

import java.util.regex.Matcher;

public class Task {

    /**
     * Une Task doit être associée à un thread client afin que l'executor puisse savoir à quel client il
     * doit renvoyer un message quand il execute cette tache (c'est pourquoi un ClientRunnable fait partie
     * des attributs d'une Task).
     */
    private ClientRunnable client;
    private final TaskType type;
    private final Matcher matcher;
    private final String info;

    public Task(TaskType type, Matcher matcher, String info){
        this.type = type;
        this.matcher = matcher;
        this.info = info;
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

    public String getInfo() {
        return info;
    }
}
