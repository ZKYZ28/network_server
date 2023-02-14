package org.helmo.murmurG6.models;

import java.util.ArrayList;
import java.util.List;

public class User {

    private final String login; //Login de l'utilisateur.
    private final BCrypt bCrypt;
    private List<FollowInformation> followedUsers; //Liste des utilisateurs suivis par l'utilisateur.
    private List<FollowInformation> followedTrends; //Liste des tendances suivies par l'utilisateur.

    public User(String login, BCrypt bCrypt) {
        this.login = login;
        this.bCrypt = bCrypt;
        this.followedUsers = new ArrayList<>();
        this.followedTrends = new ArrayList<>();
    }

    public BCrypt getBcrypt() { return this.bCrypt; }

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

    /**
     * Retourne la liste des utilisateurs suivis par l'utilisateur.
     * @return liste des utilisateurs suivis par l'utilisateur.
     */
    public List<FollowInformation> getFollowedUsers() {
        return followedUsers;
    }

    /**
     * Retourne la liste des trends suivies par l'utilisateur.
     * @return liste des trends suivies par l'utilisateur.
     */
    public List<FollowInformation> getFollowedTrends() {
        return followedTrends;
    }

    /**
     * Définit la liste des utilisateurs suivis.
     *
     * @param followedUsers Liste des utilisateurs suivis.
     */
    public void setFollowedUsers(List<FollowInformation> followedUsers) {
        this.followedUsers = followedUsers;
    }

    /**
     * Définit la liste des tendances suivies.
     *
     * @param followedTrends Liste des tendances suivies.
     */
    public void setFollowedTrends(List<FollowInformation> followedTrends) {
        this.followedTrends = followedTrends;
    }

    /**
     * Permet d'ajouter un utilisateur à sa liste d'abonnement
     * @param followUser La personne suivie: (swila)@server1.godswila.guru
     */
    public void followUser(FollowInformation followUser) {
        if (!this.followedUsers.contains(followUser)) {
            this.followedUsers.add(followUser);
        }
    }

    /**
     * Permet d'ajouter une tendance à sa liste d'abonnement
     * @param followTrend la tendance suivie i.e : #tendance1234@server1.godswila.guru
     */
    public void followTrend(FollowInformation followTrend) {
        if (!this.followedTrends.contains(followTrend)) {
            this.followedTrends.add(followTrend);
        }
    }


    public boolean chekcIfFollowUser(String login){
        return this.followedUsers.contains(new FollowInformation(login, "empty"));
    }

    public boolean chekcIfFollowTrend(FollowInformation trend){
        return this.followedTrends.contains(trend);
    }
}