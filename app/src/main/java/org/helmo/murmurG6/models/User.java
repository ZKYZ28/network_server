package org.helmo.murmurG6.models;

import java.util.ArrayList;
import java.util.List;

public class User {

    private final String login; //Login de l'utilisateur.
    private final String bcryptHash; //Empreinte bcrypt du mot de passe de l'utilisateur.
    private final int bcryptRound; //Nombre de tours bcrypt utilisé pour hacher le mot de passe.
    private final String bcryptSalt; //Sel bcrypt utilisé pour hacher le mot de passe.
    private List<String> followedUsers; //Liste des utilisateurs suivis par l'utilisateur.
    private List<String> followedTrends; //Liste des tendances suivies par l'utilisateur.

    /**
     * Constructeur pour la classe User.
     *
     * @param login Login de l'utilisateur.
     * @param bcryptHash Empreinte bcrypt du mot de passe de l'utilisateur.
     * @param bcryptRound Nombre de tours bcrypt utilisé pour hacher le mot de passe.
     * @param bcryptSalt Sel bcrypt utilisé pour hacher le mot de passe.
     */
    public User(String login, String bcryptHash, int bcryptRound, String bcryptSalt) {
        this.login = login;
        this.bcryptHash = bcryptHash;
        this.bcryptRound = bcryptRound;
        this.bcryptSalt = bcryptSalt;
        this.followedUsers = new ArrayList<>();
        this.followedTrends = new ArrayList<>();
    }

    /**
     * Retourne le login de l'utilisateur.
     * @return Login de l'utilisateur.
     */
    public String getLogin() {
        return this.login;
    }

    /**
     * Retourne l'empreinte bcrypt du mot de passe de l'utilisateur.
     * @return Empreinte bcrypt du mot de passe de l'utilisateur.
     */
    public String getBcryptHash() {
        return this.bcryptHash;
    }

    /**
     * Retourne le nombre de tours bcrypt utilisé pour hacher le mot de passe.
     * @return Nombre de tours bcrypt utilisé pour hacher le mot de passe.
     */
    public int getBcryptRound() {
        return this.bcryptRound;
    }

    /**
     * Retourne le sel bcrypt utilisé pour hacher le mot de passe.
     * @return Sel bcrypt utilisé pour hacher le mot de passe.
     */
    public String getBcryptSalt() {
        return this.bcryptSalt;
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
     * Permet de verifier que le résultat du hachage est bien égal au mot de passe haché de l'utilisateur
     * @param hashedPassword Le résultat du hachage
     * @return True si le résultat est correcte, sinon false
     */
    public boolean verifyPassword(String hashedPassword) {
        return this.bcryptHash.equals(hashedPassword);
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