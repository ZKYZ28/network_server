package org.helmo.murmurG6.repository;

import org.helmo.murmurG6.repository.exceptions.SaveUserCollectionException;
import org.helmo.murmurG6.models.UserCollection;

import java.io.IOException;

public interface IUserCollectionRepository {

    void save(UserCollection uc) throws SaveUserCollectionException;

    UserCollection read() throws IOException;
}
