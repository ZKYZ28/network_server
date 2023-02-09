package org.helmo.murmurG6.models;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ProtocolTest {

    @Test
    void checkConnect() {
        //Given
        Protocol protocol = new Protocol();
        String msgClientToServer = "CONNECT LOUIS\r\n";
        //When
        Message connectMessage = protocol.analyseMessage(msgClientToServer);
        //Then
       assertEquals(MessageType.CONNECT, connectMessage.getType());
    }
}
