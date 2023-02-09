package org.helmo.murmurG6.models;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UserTest {

    @Test
    void checkName() {
        User test = new User("dgeg", "fgdfg", 5453, "dfgdfg");
        assertEquals(test.getLogin(), "dgeg");
    }
}
