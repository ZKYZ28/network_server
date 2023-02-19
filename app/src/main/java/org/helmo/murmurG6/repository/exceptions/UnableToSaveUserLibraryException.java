package org.helmo.murmurG6.repository.exceptions;

import java.io.IOException;

public class UnableToSaveUserLibraryException extends IOException {
    public UnableToSaveUserLibraryException(String message) {
        super(message);
    }
}
