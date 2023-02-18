package org.helmo.murmurG6.models;

import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;

public class User {

    private final String login; //Login de l'utilisateur.
    private final BCrypt bCrypt;
    private final Set<String> userFollowers; //Liste des utilisateurs suivis par l'utilisateur.
    private final Set<Trend> followedTrends; //Liste des tendances suivies par l'utilisateur.

    public User(String login, BCrypt bCrypt, Set<String> userFollowers, Set<Trend> followedTrends) {
        this.login = login;
        this.bCrypt = bCrypt;
        this.userFollowers = userFollowers;
        this.followedTrends = followedTrends;
    }

    public boolean followsTrend(Set<Trend> extractedTrends) {
        for (Trend trend : extractedTrends) {
            for (Trend followedTrend : this.followedTrends) {
                if (followedTrend.contains(trend)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void addFollower(String follower) {
        this.userFollowers.add(follower);
    }

    public void followTrend(Trend trend) {
        this.followedTrends.add(trend);
    }

    /**
     * Retourne la trend (tag@domain) suivie par l'utilisateur en fonction du tag passé en paramètre
     * @param tag le tag de la trend recherché dans la liste de trend suivie par l'utilisateur
     * @return La trend
     * @throws InexistantTrendTagException Exception lancée si le tag de la trend recherché n'est pas suivi par l'utilisateur
     */
    public Trend getTrendByTag(String tag) throws InexistantTrendTagException {
        for(Trend trend: followedTrends){
            if(tag.equals(trend.getTrendName())) {
                return trend;
            }
        }
        throw new InexistantTrendTagException("Tag inexistant!");
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

    public Set<String> getUserFollowers() {
        return userFollowers;
    }

    public Set<Trend> getFollowedTrends() {
        return followedTrends;
    }

    private static class InexistantTrendTagException extends Throwable {
        public InexistantTrendTagException(String message) {
            super(message);
        }
    }
}