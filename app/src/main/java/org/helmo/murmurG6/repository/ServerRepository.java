package org.helmo.murmurG6.repository;

import org.helmo.murmurG6.controller.ServerConfig;
import org.helmo.murmurG6.repository.exceptions.UnableToLoadServerConfigurationException;

public interface ServerRepository {

     ServerConfig load() throws UnableToLoadServerConfigurationException;
}
