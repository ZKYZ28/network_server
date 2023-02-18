package org.helmo.murmurG6.repository;

import org.helmo.murmurG6.controller.ServerConfig;
import org.helmo.murmurG6.repository.exceptions.ReadServerConfigurationException;

public interface ServerRepository {

     ServerConfig loadServerConfiguration() throws ReadServerConfigurationException;
}
