package org.helmo.murmurG6.models;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class TrendLibrary extends HashMap<String, Set<UserCredentials>> {

    public void addUserToTrend(String trendName, UserCredentials userCredentials) {
        if (!this.containsKey(trendName)) {
            this.put(trendName, new HashSet<>());
        }
        this.get(trendName).add(userCredentials);
    }
}