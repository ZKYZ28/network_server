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
public class UserLibrary {

    private final Map<String, User> userMap;

    public UserLibrary() {
        this.userMap = Collections.synchronizedMap(new HashMap<>());
    }

    public void register(User user) throws UserAlreadyRegisteredException {
        if(this.userMap.containsKey(user.getLogin())){
            throw new UserAlreadyRegisteredException("L'utilisateur est déja inscrit!");
        }else{
            this.userMap.put(user.getLogin(), user);
        }
    }

    public static UserLibrary of(Iterable<User> users) {
        UserLibrary library = new UserLibrary();
        for(User u : users){
            library.userMap.put(u.getLogin(), u);
        }
        return library;
    }

    public User getUser(String login) {
        if (this.userMap.containsKey(login)) {
            return this.userMap.get(login);
        }
        return null;
    }

    public boolean isRegistered(String login) {
        return this.userMap.containsKey(login);
    }

    public Set<User> getUsers() {
        return new HashSet<>(this.userMap.values());
    }
}
