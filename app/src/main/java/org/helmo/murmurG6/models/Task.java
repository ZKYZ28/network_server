package org.helmo.murmurG6.models;

import java.util.regex.Matcher;

public class Task {
    private final TaskType type;
    private final Matcher matcher;
    private final String msg;

    public Task(TaskType type, Matcher matcher, String msg){
        this.type = type;
        this.matcher = matcher;
        this.msg = msg;
    }

    public TaskType getType() {
        return type;
    }

    public Matcher getMatcher() {
        return matcher;
    }

    public String getMsg() {
        return msg;
    }

}
