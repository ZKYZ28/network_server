package org.helmo.murmurG6.models;

import org.helmo.murmurG6.controller.ClientRunnable;

public class Task {

    private final String taskId;            //L'identifiant de la tache (pour éviter les doublons)
    private final ClientRunnable client;    //Le thread de l'emetteur de la tache
    private final UserCredentials sender;   //Les creditentials de l'emetteur de la tache
    private final String receiver;          //Les creditentials du destinataire de la tache
    private final TaskType type;            //Le type de la tache
    private final String content;           //Le contenu de la tache


    public Task(String taskId, ClientRunnable client, UserCredentials sender, String receiver, TaskType type, String content) {
        this.taskId = taskId;
        this.client = client;
        this.sender = sender;
        this.receiver = receiver;
        this.type = type;
        this.content = content;
    }

    public String getTaskId() {
        return taskId;
    }

    public ClientRunnable getClient() {
        return client;
    }

    public UserCredentials getSender() {
        return sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public TaskType getType() {
        return type;
    }

    public String getContent() {
        return content;
    }
}
