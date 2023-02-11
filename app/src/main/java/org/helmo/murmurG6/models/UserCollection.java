package org.helmo.murmurG6.models;



import org.helmo.murmurG6.models.exceptions.UserAlreadyRegisteredException;

import java.util.*;

public class UserCollection {

    public Map<String, User> registeredUsers;

    public Iterable<User> getRegisteredUsers() {
        return new ArrayList<>(this.registeredUsers.values());
    }

    public void setRegisteredUsers(Iterable<User> users) {
        this.registeredUsers = Collections.synchronizedMap(new HashMap<>());
        for (User user : users) {
            registeredUsers.put(user.getLogin(), user);
        }
    }

    public void registerUser(User user) throws UserAlreadyRegisteredException {
        if (isRegistered(user)) {
            throw new UserAlreadyRegisteredException("Un utilisateur est déjà inscrit sous ce login.");
        } else {
            this.registeredUsers.put(user.getLogin(), user);
        }
    }

    private boolean isRegistered(User user) {
        return this.registeredUsers.containsKey(user.getLogin());
    }
}
