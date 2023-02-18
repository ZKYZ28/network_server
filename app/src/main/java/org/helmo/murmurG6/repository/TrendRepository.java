package org.helmo.murmurG6.repository;

import org.helmo.murmurG6.models.TrendLibrary;
import org.helmo.murmurG6.models.UserLibrary;
import org.helmo.murmurG6.repository.exceptions.ReadUserCollectionException;
import org.helmo.murmurG6.repository.exceptions.SaveUserCollectionException;

public interface TrendRepository {

    void save(TrendLibrary library) throws SaveUserCollectionException;

    TrendLibrary load() throws ReadUserCollectionException;
}
