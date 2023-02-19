package org.helmo.murmurG6.controller.exceptions;

public class UnableToExecuteTaskException extends RuntimeException {

    public UnableToExecuteTaskException(String s, Exception e) {
        super(s, e);
    }

}
