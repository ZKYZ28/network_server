package org.helmo.murmurG6.repository;

import org.helmo.murmurG6.models.ServerConfig;
import org.helmo.murmurG6.repository.exceptions.ReadServerConfigurationException;
import org.helmo.murmurG6.repository.exceptions.SaveUserCollectionException;

public interface ServerRepository {

     ServerConfig loadServerConfiguration() throws ReadServerConfigurationException;
}
