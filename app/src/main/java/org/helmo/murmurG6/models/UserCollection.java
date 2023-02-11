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

    private Map<String, User> registeredUsers;

    public Map<String, User> getRegisteredUsers() {
        return registeredUsers;
    }

    public void setRegisteredUsers(Iterable<User> users) {
        this.registeredUsers = Collections.synchronizedMap(new HashMap<>());
        for (User user : users) {
            registeredUsers.put(user.getLogin(), user);
        }
    }

    public void registerUser(User user) throws UserAlreadyRegisteredException {
        if (isRegistered(user.getLogin())) {
            throw new UserAlreadyRegisteredException("Un utilisateur est déjà inscrit sous ce login.");
        } else {
            this.registeredUsers.put(user.getLogin(), user);
        }
    }

    public boolean isRegistered(String loggin) {
        return this.registeredUsers.containsKey(loggin);
    }


    public User getUserFromLogin(String loggin){
        return this.registeredUsers.get(loggin);
    }
}
