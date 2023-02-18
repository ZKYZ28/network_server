package org.helmo.murmurG6.models;

import java.util.List;
import java.util.Set;

public class User {

    private final String login; //Login de l'utilisateur.
    private final BCrypt bCrypt;
    private final List<String> userFollowers; //Liste des utilisateurs suivis par l'utilisateur.
    private final List<String> followedTrends; //Liste des tendances suivies par l'utilisateur.

    public User(String login, BCrypt bCrypt, List<String> userFollowers, List<String> followedTrends) {
        this.login = login;
        this.bCrypt = bCrypt;
        this.userFollowers = userFollowers;
        this.followedTrends = followedTrends;
    }

    public boolean followsTrend(Set<String> extractedTrends) {
        for (String trend : extractedTrends) {
            for (String followedTrend : this.followedTrends) {
                if (followedTrend.contains(trend)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void addFollower(String follower) {
        if (!this.userFollowers.contains(follower)) {
            this.userFollowers.add(follower);
        }
    }

    public void followTrend(String trendId) {
        if (!this.followedTrends.contains(trendId)) {
            this.followedTrends.add(trendId);
        }
    }



    /***************** GETTERS *****************/

    public BCrypt getBcrypt() {
        return this.bCrypt;
    }

    public String getLogin() {
        return this.login;
    }

    public String getBcryptHash() {
        return this.bCrypt.getHash();
    }

    public int getBcryptRound() {
        return this.bCrypt.getRounds();
    }

    public String getBcryptSalt() {
        return this.bCrypt.getSalt();
    }

    public List<String> getUserFollowers() {
        return userFollowers;
    }

    public List<String> getFollowedTrends() {
        return followedTrends;
    }
}