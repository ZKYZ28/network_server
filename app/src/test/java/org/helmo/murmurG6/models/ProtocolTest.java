package org.helmo.murmurG6.models;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


class ProtocolTest {

    @Test
    void checkConnectInTheRightCase() {
        //Given
        Protocol protocol = new Protocol();
        String msgClientToServer = "CONNECT LOUIS\r\n";
        //When
        Message connectMessage = protocol.analyseMessage(msgClientToServer);
        //Then
       assertEquals(MessageType.CONNECT, connectMessage.getType());
    }

    @Test
    void checkConnectInBadCase() {
        //Given
        Protocol protocol = new Protocol();
        String msgClientToServer = "CONNECT @@123abc\r\n";
        //When
        Message connectMessage = protocol.analyseMessage(msgClientToServer);
        //Then
        assertEquals(MessageType.MESSAGE, connectMessage.getType());
    }

    @Test
    void registerInTheRightCase() {
        //Given
        Protocol protocol = new Protocol();
        String msgClientToServer = "REGISTER LOUIS 22 $2b$14$gfuefhsivdiv18\r\n";
        //When
        Message msg = protocol.analyseMessage(msgClientToServer);
        //Then
        assertEquals(MessageType.REGISTER, msg.getType());

        //When
        msgClientToServer = "REGISTER LOUIS 22 $2b$16$gfuefhsivdiv18\r\n";
        msg = protocol.analyseMessage(msgClientToServer);
        //Then
        assertEquals(MessageType.REGISTER, msg.getType());

        //When
        msgClientToServer = "REGISTER LOUIS 22 $2b$14$grgsdgrgrgrvfc@()*+,-./\r\n";
        msg = protocol.analyseMessage(msgClientToServer);
        //Then
        assertEquals(MessageType.REGISTER, msg.getType());

        //When
        msgClientToServer = "REGISTER LOUIS 22 $2b$14$grgsdgrgrgrvfc@()*+,-./\r\n";
        msg = protocol.analyseMessage(msgClientToServer);
        //Then
        assertEquals(MessageType.REGISTER, msg.getType());
    }

    @Test
    void registerInBadCase() {
        //Given
        Protocol protocol = new Protocol();
        String msgClientToServer = "REGISTER LOUIS 1 $2b$14$trefvrgtgbgrvfcezf\r\n";
        //When
        Message msg = protocol.analyseMessage(msgClientToServer);
        //Then
        assertEquals(MessageType.MESSAGE, msg.getType());

        //When
        msgClientToServer = "REGISTER LOUIS 22 $2b14$trarefefefzfzzf\r\n";
        msg = protocol.analyseMessage(msgClientToServer);
        //Then
        assertEquals(MessageType.MESSAGE, msg.getType());

        //When
        msgClientToServer = "REGISTER LOUIS 22 $3b$14$grgsdgrgrgrvfc\r\n";
        msg = protocol.analyseMessage(msgClientToServer);
        //Then
        assertEquals(MessageType.MESSAGE, msg.getType());

    }
}
