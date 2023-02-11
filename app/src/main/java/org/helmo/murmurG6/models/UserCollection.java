package org.helmo.murmurG6.models;



import org.helmo.murmurG6.models.exceptions.UserAlreadyRegisteredException;

import java.util.*;

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


    public User getUserOnLoggin(String loggin){
        return this.registeredUsers.get(loggin);
    }
}
