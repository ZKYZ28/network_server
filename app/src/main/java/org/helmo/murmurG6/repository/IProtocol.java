package org.helmo.murmurG6.repository;

import org.helmo.murmurG6.models.Message;

public interface IProtocol {

    Message analyseMessage(String msg);
}
