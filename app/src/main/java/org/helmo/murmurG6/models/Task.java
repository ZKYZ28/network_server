package org.helmo.murmurG6.models;

import org.helmo.murmurG6.controller.ClientRunnable;

public class Task {

    private final ClientRunnable client;
    private final TaskType type;
    private final String content;

    public Task(ClientRunnable client, TaskType type, String content) {
        this.client = client;
        this.type = type;
        this.content = content;
    }

    public ClientRunnable getClient() {
        return client;
    }

    public TaskType getType() {
        return type;
    }

    public String getContent() {
        return content;
    }
}
