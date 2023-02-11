package org.helmo.murmurG6.models;

import java.util.regex.Matcher;

public class Message {
    private final MessageType type;
    private final Matcher matcher;
    private final String msg;

    public Message(MessageType type, Matcher matcher, String msg){
        this.type = type;
        this.matcher = matcher;
        this.msg = msg;
    }

    public MessageType getType() {
        return type;
    }

    public Matcher getMatcher() {
        return matcher;
    }

    public String getMsg() {
        return msg;
    }

}
