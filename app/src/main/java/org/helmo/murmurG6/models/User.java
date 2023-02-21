package org.helmo.murmurG6.models;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class User {

    private final UserCredentials userCredentials; //Login de l'utilisateur.
    private final BCrypt bCrypt;
    private final Set<UserCredentials> userFollowers; //Liste des utilisateurs suivis par l'utilisateur.
    private final Set<Trend> followedTrends; //Liste des tendances suivies par l'utilisateur.

    private final Set<String> messageIdHistory = new HashSet<>();

    public User(UserCredentials userCredentials, BCrypt bCrypt, Set<UserCredentials> userFollowers, Set<Trend> followedTrends) {
        this.userCredentials = userCredentials;
        this.bCrypt = bCrypt;
        this.userFollowers = userFollowers;
        this.followedTrends = followedTrends;
    }

    /**
     * @param extractedTrends Un set de trends contenu dans un messages (Ex: Message = Yo les gars #devs #yo #salut, le Set contiendrait #devs, #yo, et #salut)
     * @return True si au moins une des trends du set est parmis les trend suivies par l'utilisateur
     */
    public boolean followsTrend(Set<Trend> extractedTrends) {
        for (Trend trend : extractedTrends) { //On parcours les trends du set (#devs, #yo, et #salut)
            if (this.followedTrends.contains(trend)) {
                return true;
            }
        }
        return false;
    }

    public void addFollower(UserCredentials follower) {
        this.userFollowers.add(follower);
    }

    public void followTrend(Trend trend) {
        this.followedTrends.add(trend);
    }

    /**
     * Retourne la trend (tag@domain) suivie par l'utilisateur en fonction du tag passé en paramètre
     *
     * @param tag le tag de la trend recherché dans la liste de trend suivie par l'utilisateur
     * @return La trend
     */
    public Trend getTrendByTag(String tag) {
        for (Trend trend : followedTrends) {
            if (tag.equals(trend.getTrendName())) {
                return trend;
            }
        }
        return null;
    }


    /***************** GETTERS *****************/

    public BCrypt getBcrypt() {
        return this.bCrypt;
    }

    public String getLogin() {
        return this.userCredentials.getLogin();
    }

    public UserCredentials getCredentials(){
        return this.userCredentials;
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

    public Set<UserCredentials> getUserFollowers() {
        return userFollowers;
    }

    public Set<Trend> getFollowedTrends() {
        return followedTrends;
    }

    @Override
    public String toString() {
        return this.userCredentials.toString();
    }

    public boolean hasAlreadyReceivedMessage(String idMessage) {
        return this.messageIdHistory.contains(idMessage);
    }

    public void saveReceivedMessageId(String idMessage) {
        this.messageIdHistory.add(idMessage);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return userCredentials.equals(user.userCredentials);
    }

}
