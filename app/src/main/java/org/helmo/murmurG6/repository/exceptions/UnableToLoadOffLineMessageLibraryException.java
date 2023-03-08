package org.helmo.murmurG6.repository.exceptions;

import java.io.IOException;

public class UnableToLoadOffLineMessageLibraryException extends IOException {

    public UnableToLoadOffLineMessageLibraryException(String message){
        super(message);
    }
}
