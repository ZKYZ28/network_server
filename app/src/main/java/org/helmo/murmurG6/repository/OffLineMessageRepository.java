package org.helmo.murmurG6.repository;

import org.helmo.murmurG6.infrastructure.dto.OffLineMessageDto;
import org.helmo.murmurG6.models.OffLineMessage;
import org.helmo.murmurG6.models.UserCredentials;
import org.helmo.murmurG6.repository.exceptions.UnableToLoadOffLineMessageLibraryException;
import org.helmo.murmurG6.repository.exceptions.UnableToSaveOffLineMessageLibraryException;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public interface OffLineMessageRepository {

    void save(Map<String, TreeSet<OffLineMessage>> message) throws UnableToSaveOffLineMessageLibraryException;

    Map<String, TreeSet<OffLineMessage>> load() throws UnableToLoadOffLineMessageLibraryException;
}
