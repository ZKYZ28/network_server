package org.helmo.murmurG6.repository;

import org.helmo.murmurG6.models.User;
import org.helmo.murmurG6.models.UserLibrary;
import org.helmo.murmurG6.repository.exceptions.ReadUserCollectionException;
import org.helmo.murmurG6.repository.exceptions.SaveUserCollectionException;

import java.io.IOException;
import java.util.Map;

public interface UserRepository<S, U> {

    void save(Map<S, U> uc) throws SaveUserCollectionException;

    UserLibrary load() throws ReadUserCollectionException;
}
