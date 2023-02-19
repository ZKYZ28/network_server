package org.helmo.murmurG6.controller.exceptions;

public class UnableToRunClientException extends RuntimeException {
    public UnableToRunClientException(String s, Exception e) {
        super(s, e);
    }
}
