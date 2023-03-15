package org.helmo.murmurG6.models;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

public class OfflineMessagesLibrary {

    Map<String, TreeMap<String, OffLineMessage>> offlineMessages;

    public OfflineMessagesLibrary(Map<String, TreeMap<String, OffLineMessage>> offlineMessages){
        this.offlineMessages = offlineMessages;
    }



    public Map<String, TreeMap<String, OffLineMessage>> getOfflineMessages() {
        return new HashMap<>(offlineMessages);
    }

    public void addOfflineMessage(String userDomain, OffLineMessage offLineMessage){
        if(!offlineMessages.containsKey(userDomain)) {
            offlineMessages.put(userDomain, new TreeMap<>());
        }
        offlineMessages.get(userDomain).put(offLineMessage.getMessageId(), offLineMessage);
    }

    public boolean existOfflineMessagesForUser(String userDomain){
        return offlineMessages.containsKey(userDomain);
    }

    public Iterable<OffLineMessage> getOfflineMessagesForUser(String userDomain){
        return offlineMessages.get(userDomain).values();
    }

    public void deleteOfflineMessagesForUser(String userDomain){
        offlineMessages.remove(userDomain);
    }
}
