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
public class UserLibrary extends HashMap<String, User> {

    public void register(User user) throws UserAlreadyRegisteredException {
        if(this.containsKey(user.getLogin())){
            throw new UserAlreadyRegisteredException("L'utilisateur est déja inscrit!");
        }else{
            this.put(user.getLogin(), user);
        }
    }

    public static UserLibrary of(Iterable<User> users) {
        UserLibrary ulib = new UserLibrary();
        for(User u : users){
            ulib.put(u.getLogin(), u);
        }
        return ulib;
    }
}
