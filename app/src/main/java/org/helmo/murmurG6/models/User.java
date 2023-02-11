package org.helmo.murmurG6.models;

import java.util.ArrayList;
import java.util.List;

public class User {

    private final String login; //Login de l'utilisateur.
    private final BCrypt BCrypt;
    private List<String> followedUsers; //Liste des utilisateurs suivis par l'utilisateur.
    private List<String> followedTrends; //Liste des tendances suivies par l'utilisateur.

    public User(String login, BCrypt BCrypt) {
        this.login = login;
        this.BCrypt = BCrypt;
        this.followedUsers = new ArrayList<>();
        this.followedTrends = new ArrayList<>();
    }

    public BCrypt getBcrypt() { return this.BCrypt; }

    public String getLogin() {
        return this.login;
    }

    public String getBcryptHash() {
        return this.BCrypt.getHash();
    }

    public int getBcryptRound() {
        return this.BCrypt.getRounds();
    }

    public String getBcryptSalt() {
        return this.BCrypt.getSalt();
    }

    /**
     * Retourne la liste des utilisateurs suivis par l'utilisateur.
     * @return liste des utilisateurs suivis par l'utilisateur.
     */
    public List<String> getFollowedUsers() {
        return followedUsers;
    }

    /**
     * Retourne la liste des trends suivies par l'utilisateur.
     * @return liste des trends suivies par l'utilisateur.
     */
    public List<String> getFollowedTrends() {
        return followedTrends;
    }

    /**
     * Définit la liste des utilisateurs suivis.
     *
     * @param followedUsers Liste des utilisateurs suivis.
     */
    public void setFollowedUsers(List<String> followedUsers) {
        this.followedUsers = followedUsers;
    }

    /**
     * Définit la liste des tendances suivies.
     *
     * @param followedTrends Liste des tendances suivies.
     */
    public void setFollowedTrends(List<String> followedTrends) {
        this.followedTrends = followedTrends;
    }

    /**
     * Permet d'ajouter un utilisateur à sa liste d'abonnement
     * @param userId L'id de l'utilisateur i.e : swila@server1.godswila.guru
     */
    public void followUser(String userId) {
        if (!this.followedUsers.contains(userId)) {
            this.followedUsers.add(userId);
        }
    }

    /**
     * Permet d'ajouter une tendance à sa liste d'abonnement
     * @param trendId L'id de l'utilisateur i.e : #tendance1234@server1.godswila.guru
     */
    public void followTrend(String trendId) {
        if (!this.followedTrends.contains(trendId)) {
            this.followedUsers.add(trendId);
        }
    }
}