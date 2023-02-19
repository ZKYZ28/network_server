package org.helmo.murmurG6.controller;

import org.helmo.murmurG6.models.UserCredentials;

import java.util.*;

public class MessageHistoryBuffer {

    private final Map<UUID, HashSet<String>> messagesHistory = new HashMap<>();

    public void createMessageHistory(UUID uuid){
        messagesHistory.put(uuid, new HashSet<>());
    }

    public void addUsertoHistory(UUID uuid, String userName){
        messagesHistory.get(uuid).add(userName);
    }

    public boolean hasAlreadyReceived(UUID uuid, String userName){
        return messagesHistory.get(uuid).contains(userName);
    }
}
