package org.helmo.murmurG6.repository;

import org.helmo.murmurG6.models.Task;

public interface IProtocol {

    Task analyseMessage(String msg);
}
