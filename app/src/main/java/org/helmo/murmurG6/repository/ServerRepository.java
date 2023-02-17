package org.helmo.murmurG6.repository;

import org.helmo.murmurG6.repository.exceptions.SaveUserCollectionException;

public interface ServerRepository {

    String loadKeyAes() throws SaveUserCollectionException;
}
