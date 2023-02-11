package org.helmo.murmurG6.models;

import org.helmo.murmurG6.models.exceptions.UserAlreadyRegisteredException;
import java.util.*;

/**
 * La classe UserCollection est une classe qui permet de gérer une collection d'utilisateurs enregistrés.
 * Elle utilise une implémentation de la classe `HashMap` pour stocker les utilisateurs enregistrés,
 * ce qui permet de rechercher les utilisateurs enregistrés en utilisant leur nom d'utilisateur (login).
 *
 * @version 1.0
 * @since 11 février 2023
 */
public class UserCollection {

    private Map<String, User> registeredUsers; //L'ensemble des utilisateur inscrit (KEY: loggin -- VALUE: objet User)

    /**
     * Retourne la map d'utilisateur inscrit sur le server
     * @return la map des user inscrits
     */
    public Map<String, User> getRegisteredUsers() {
        return registeredUsers;
    }

    /**
     * Rempli la map registeredUsers à partir d'un iterable contenant des User
     * @param users des utilisateurs
     */
    public void setRegisteredUsers(Iterable<User> users) {
        this.registeredUsers = Collections.synchronizedMap(new HashMap<>());
        for (User user : users) {
            registeredUsers.put(user.getLogin(), user);
        }
    }

    /**
     * Enregistre un utilisateur dans la collection d'utilisateur inscrits sur le server
     * @param user un User
     * @throws UserAlreadyRegisteredException
     */
    public void registerUser(User user) throws UserAlreadyRegisteredException {
        if (isRegistered(user.getLogin())) {
            throw new UserAlreadyRegisteredException("Un utilisateur est déjà inscrit sous ce login.");
        } else {
            this.registeredUsers.put(user.getLogin(), user);
        }
    }

    /**
     * Vérrifie si un utilisateur est déja connecté
     * @param login Le loggin de l'utilisateur pour qui on regarde si il est déja inscrit
     * @return True si il est déja inscrit, false sinon
     */
    public boolean isRegistered(String login) {
        return this.registeredUsers.containsKey(login);
    }


    /**
     * Recupere un User dans la collection d'utilisateur inscrits sur le server sur base de leur loggin
     * @param login Le loggin de l'utlisateur que l'on souhaite récupérer
     * @return L'utilisateur que l'on cherche
     */
    public User getUserFromLogin(String login) {
        return this.registeredUsers.get(login);
    }
}
