package org.helmo.murmurG6.repository;

import org.helmo.murmurG6.repository.exceptions.UnableToSaveTrendLibraryException;
import org.helmo.murmurG6.repository.exceptions.UnableToLoadTrendLibraryException;
import org.helmo.murmurG6.models.TrendLibrary;
import org.helmo.murmurG6.repository.exceptions.UnableToLoadUserLibraryException;
import org.helmo.murmurG6.repository.exceptions.UnableToSaveUserLibraryException;

public interface TrendRepository {

    void save(TrendLibrary library) throws UnableToSaveUserLibraryException, UnableToSaveTrendLibraryException;

    TrendLibrary load() throws UnableToLoadUserLibraryException, UnableToLoadTrendLibraryException;
}
