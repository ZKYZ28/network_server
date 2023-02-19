package org.helmo.murmurG6.repository;

import org.helmo.murmurG6.models.UserLibrary;
import org.helmo.murmurG6.repository.exceptions.UnableToLoadUserLibraryException;
import org.helmo.murmurG6.repository.exceptions.UnableToSaveUserLibraryException;

public interface UserRepository {

    void save(UserLibrary uc) throws UnableToSaveUserLibraryException;

    UserLibrary load() throws UnableToLoadUserLibraryException;
}
