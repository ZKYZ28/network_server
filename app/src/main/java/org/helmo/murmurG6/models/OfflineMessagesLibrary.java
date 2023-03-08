package org.helmo.murmurG6.models;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

public class OfflineMessagesLibrary {

    Map<String, TreeSet<OffLineMessage>> offlineMessages;

    public OfflineMessagesLibrary(Map<String, TreeSet<OffLineMessage>> offlineMessages){
        this.offlineMessages = offlineMessages;
    }

    public Map<String, TreeSet<OffLineMessage>> getOfflineMessages() {
        return new HashMap<String, TreeSet<OffLineMessage>>(offlineMessages);
    }

    public void addOfflineMessage(String userDomain, OffLineMessage offLineMessage){
        if(!offlineMessages.containsKey(userDomain)) {
            offlineMessages.put(userDomain, new TreeSet<>());
        }
        offlineMessages.get(userDomain).add(offLineMessage);
    }

    public boolean existOfflineMessagesForUser(String userDomain){
        return offlineMessages.containsKey(userDomain);
    }

    public TreeSet<OffLineMessage> getOfflineMessagesForUser(String userDomain){
        return offlineMessages.get(userDomain);
    }

    public void deleteOfflineMessagesForUser(String userDomain){
        offlineMessages.remove(userDomain);
    }
}
