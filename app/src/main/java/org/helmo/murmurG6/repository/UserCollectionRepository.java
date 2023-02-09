package org.helmo.murmurG6.repository;

import org.helmo.murmurG6.infrastructure.storage.json.ReadUserCollectionException;
import org.helmo.murmurG6.infrastructure.storage.json.SaveUserCollectionException;
import org.helmo.murmurG6.models.UserCollection;

public interface UserCollectionRepository {

    void save(UserCollection uc) throws SaveUserCollectionException;

    UserCollection read() throws ReadUserCollectionException;
}
