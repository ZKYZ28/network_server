package org.helmo.murmurG6.repository.exceptions;

import java.io.IOException;

public class UnableToSaveOffLineMessageLibraryException extends IOException {

    public UnableToSaveOffLineMessageLibraryException(String message){
        super(message);
    }
}
