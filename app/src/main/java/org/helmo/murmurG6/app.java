package org.helmo.murmurG6;

import org.helmo.murmurG6.server.ServerController;
import java.io.IOException;

public class app {

    private static final int DEFAULT_PORT = 12345;
    public static void main(String[] args) {
        try (ServerController server = new ServerController(DEFAULT_PORT)) {

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
