package org.helmo.murmurG6.models;

import java.util.*;

public class TrendLibrary {

    private final Map<String, Set<UserCredentials>> trendMap;

    public TrendLibrary() {
        this.trendMap = Collections.synchronizedMap(new HashMap<>());
    }

    public void addUserToTrend(String trendName, UserCredentials user) {
        if (!trendMap.containsKey(trendName)) {
            trendMap.put(trendName, new HashSet<>());
        }
        trendMap.get(trendName).add(user);
    }

    public boolean exists(String trendName){
        return this.trendMap.containsKey(trendName);
    }

    public Map<String, Set<UserCredentials>> getTrendMap() {
        return trendMap;
    }

    public Set<UserCredentials> getUsersForTrend(String trendName) {
        return this.trendMap.get(trendName);
    }
}