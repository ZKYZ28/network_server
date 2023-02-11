package org.helmo.murmurG6.repository;

import org.helmo.murmurG6.models.User;
import org.helmo.murmurG6.repository.exceptions.SaveUserCollectionException;
import org.helmo.murmurG6.models.UserCollection;

import java.io.IOException;
import java.util.List;

public interface IUserCollectionRepository {

    void save(Iterable<User> uc) throws SaveUserCollectionException;

    List<User> read() throws IOException;
}
