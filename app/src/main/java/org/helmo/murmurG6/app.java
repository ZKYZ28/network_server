package org.helmo.murmurG6;

import org.helmo.murmurG6.server.ServerController;

public class app {

    private static final int DEFAULT_PORT = 12345;
    public static void main(String[] args){
        new ServerController(DEFAULT_PORT);
    }
}
