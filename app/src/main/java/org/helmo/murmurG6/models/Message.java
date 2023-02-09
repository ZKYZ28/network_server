package org.helmo.murmurG6.models;

import java.util.regex.Matcher;

public class Message {
    private MessageType type;
    private Matcher matcher;
    private String msg;

    public Message(MessageType type, Matcher matcher, String msg){
        this.type = type;
        this.matcher = matcher;
        this.msg = msg;
    }
}
