package org.helmo.murmurG6.repository;

import org.helmo.murmurG6.models.UserLibrary;
import org.helmo.murmurG6.repository.exceptions.ReadUserCollectionException;
import org.helmo.murmurG6.repository.exceptions.SaveUserCollectionException;

public interface UserRepository {

    void save(UserLibrary uc) throws SaveUserCollectionException;

    UserLibrary load() throws ReadUserCollectionException;
}
