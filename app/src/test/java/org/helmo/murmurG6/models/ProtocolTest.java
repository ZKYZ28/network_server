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
        Task connectTask = protocol.buildTask(msgClientToServer);
        //Then
       assertEquals(TaskType.CONNECT, connectTask.getType());
       assertEquals("CONNECT LOUIS\r\n", connectTask.getMatcher().group(0));
       assertEquals("LOUIS", connectTask.getMatcher().group(1));
    }

    @Test
    void checkConnectInBadCase() {
        //Given
        Protocol protocol = new Protocol();
        String msgClientToServer = "CONNECT @@123abc\r\n";
        //When
        Task connectTask = protocol.buildTask(msgClientToServer);
        //Then
        assertEquals(TaskType.MESSAGE, connectTask.getType());
    }

    @Test
    void registerInTheRightCase() {
        //Given
        Protocol protocol = new Protocol();
        String msgClientToServer = "REGISTER LOUIS 22 $2b$14$gfuefhsivdiv18\r\n";
        //When
        Task msg = protocol.buildTask(msgClientToServer);
        //Then
        assertEquals(TaskType.REGISTER, msg.getType());
        assertEquals("REGISTER LOUIS 22 $2b$14$gfuefhsivdiv18\r\n", msg.getMatcher().group(0));
        assertEquals("LOUIS", msg.getMatcher().group(1));
        assertEquals("22", msg.getMatcher().group(3));
        assertEquals("$2b$14$gfuefhsivdiv18", msg.getMatcher().group(4));

        //When
        msgClientToServer = "REGISTER LOUIS 22 $2b$16$gfuefhsivdiv18\r\n";
        msg = protocol.buildTask(msgClientToServer);
        //Then
        assertEquals(TaskType.REGISTER, msg.getType());

        //When
        msgClientToServer = "REGISTER LOUIS 22 $2b$14$grgsdgrgrgrvfc@()*+,-./\r\n";
        msg = protocol.buildTask(msgClientToServer);
        //Then
        assertEquals(TaskType.REGISTER, msg.getType());
        assertEquals("REGISTER LOUIS 22 $2b$14$grgsdgrgrgrvfc@()*+,-./\r\n", msg.getMatcher().group(0));
        assertEquals("LOUIS", msg.getMatcher().group(1));
        assertEquals("22", msg.getMatcher().group(3));
        assertEquals("$2b$14$grgsdgrgrgrvfc@()*+,-./", msg.getMatcher().group(4));

        //When
        msgClientToServer = "REGISTER LOUIS 22 $2b$14$grgsdgrgrgrvfc@()*+,-./\r\n";
        msg = protocol.buildTask(msgClientToServer);
        //Then
        assertEquals(TaskType.REGISTER, msg.getType());
    }

    @Test
    void registerInBadCase() {
        //Given
        Protocol protocol = new Protocol();
        String msgClientToServer = "REGISTER LOUIS 1 $2b$14$trefvrgtgbgrvfcezf\r\n";
        //When
        Task msg = protocol.buildTask(msgClientToServer);
        //Then
        assertEquals(TaskType.MESSAGE, msg.getType());

        //When
        msgClientToServer = "REGISTER LOUIS 22 $2b14$trarefefefzfzzf\r\n";
        msg = protocol.buildTask(msgClientToServer);
        //Then
        assertEquals(TaskType.MESSAGE, msg.getType());

        //When
        msgClientToServer = "REGISTER LOUIS 22 $3b$14$grgsdgrgrgrvfc\r\n";
        msg = protocol.buildTask(msgClientToServer);
        //Then
        assertEquals(TaskType.MESSAGE, msg.getType());

    }

    @Test
    void followInstructionInTheRightCase() {
        //Given
        Protocol protocol = new Protocol();
        String msgClientToServer = "FOLLOW lswinnen@server1.godswila.guru\r\n";
        //When
        Task connectTask = protocol.buildTask(msgClientToServer);
        //Then
        assertEquals(TaskType.FOLLOW, connectTask.getType());
        assertEquals("FOLLOW lswinnen@server1.godswila.guru\r\n", connectTask.getMatcher().group(0));
        assertEquals("lswinnen@server1.godswila.guru", connectTask.getMatcher().group(1));

        //When
        msgClientToServer = "FOLLOW swila@server2.godswila.guru\r\n";
        connectTask = protocol.buildTask(msgClientToServer);
        //Then
        assertEquals(TaskType.FOLLOW, connectTask.getType());
        assertEquals("FOLLOW swila@server2.godswila.guru\r\n", connectTask.getMatcher().group(0));
        assertEquals("swila@server2.godswila.guru", connectTask.getMatcher().group(1));

        //When
        msgClientToServer = "FOLLOW #swila123@server2.godswila.guru\r\n";
        connectTask = protocol.buildTask(msgClientToServer);
        //Then
        assertEquals(TaskType.FOLLOW, connectTask.getType());
        assertEquals("FOLLOW #swila123@server2.godswila.guru\r\n", connectTask.getMatcher().group(0));
        assertEquals("#swila123@server2.godswila.guru", connectTask.getMatcher().group(1));


    }

    @Test
    void followInstructionInBadCase() {
        //Given
        Protocol protocol = new Protocol();
        String msgClientToServer = "FOLLOW ^^^^^^^^@server1.godswila.guru\r\n";
        //When
        Task connectTask = protocol.buildTask(msgClientToServer);
        //Then
        assertEquals(TaskType.MESSAGE, connectTask.getType());

        //When
        msgClientToServer = "FOLLOW #swila12315145541441515154151541515144515414@server2.godswila.guru\r\n";
        connectTask = protocol.buildTask(msgClientToServer);
        //Then
        assertEquals(TaskType.MESSAGE, connectTask.getType());
    }

    @Test
    void checkConfirmInTheRightCase() {
        //Given
        Protocol protocol = new Protocol();
        String msgClientToServer = "CONFIRM 20de15er87er56er78er89er56er23er45er78er45er12er45er78er49er87re96sf55vv3zfheoefz486rezzefyezyfgezyvfu\r\n";
        //When
        Task connectTask = protocol.buildTask(msgClientToServer);
        //Then
        assertEquals(TaskType.CONFIRM, connectTask.getType());
        assertEquals("CONFIRM 20de15er87er56er78er89er56er23er45er78er45er12er45er78er49er87re96sf55vv3zfheoefz486rezzefyezyfgezyvfu\r\n", connectTask.getMatcher().group(0));
        assertEquals("20de15er87er56er78er89er56er23er45er78er45er12er45er78er49er87re96sf55vv3zfheoefz486rezzefyezyfgezyvfu", connectTask.getMatcher().group(1));

        //When
        msgClientToServer = "CONFIRM YSrB7bhp9ksJUqp3ifKSm1Dozi2GKUQQ2fQCKrflajRD9CgQH6VyfzVTHgdFyorPYEcmXkwSOuHbvf44MJ4BgZ0TrnEIHj5Vo839\r\n";
        connectTask = protocol.buildTask(msgClientToServer);
        //Then
        assertEquals(TaskType.CONFIRM, connectTask.getType());

        //When
        msgClientToServer = "CONFIRM YSrB7bhp9ksJUqp3ifKSm1Dozi2GKUQQ2fQCKrflajRD9CgQH6VyfzVTHgdFyorPYEcmXkwSOuHbvf44MJ4BgZ0TrnEIHj5Vo839YSrB7bhp9ksJUqp3ifKSm1Dozi2GKUQQ2fQCKrflajRD9CgQH6VyfzVTHgdFyorPYEcmXkwSOuHbvf44MJ4BgZ0TrnEIHj5Vo839\r\n";
        connectTask = protocol.buildTask(msgClientToServer);
        //Then
        assertEquals(TaskType.CONFIRM, connectTask.getType());
        assertEquals("CONFIRM YSrB7bhp9ksJUqp3ifKSm1Dozi2GKUQQ2fQCKrflajRD9CgQH6VyfzVTHgdFyorPYEcmXkwSOuHbvf44MJ4BgZ0TrnEIHj5Vo839YSrB7bhp9ksJUqp3ifKSm1Dozi2GKUQQ2fQCKrflajRD9CgQH6VyfzVTHgdFyorPYEcmXkwSOuHbvf44MJ4BgZ0TrnEIHj5Vo839\r\n", connectTask.getMatcher().group(0));
        assertEquals("YSrB7bhp9ksJUqp3ifKSm1Dozi2GKUQQ2fQCKrflajRD9CgQH6VyfzVTHgdFyorPYEcmXkwSOuHbvf44MJ4BgZ0TrnEIHj5Vo839YSrB7bhp9ksJUqp3ifKSm1Dozi2GKUQQ2fQCKrflajRD9CgQH6VyfzVTHgdFyorPYEcmXkwSOuHbvf44MJ4BgZ0TrnEIHj5Vo839", connectTask.getMatcher().group(1));

        //When
        msgClientToServer = "CONFIRM w5hQjAvqPntVAdA4VSNw3LWZTLfUKO\r\n";
        connectTask = protocol.buildTask(msgClientToServer);
        //Then
        assertEquals(TaskType.CONFIRM, connectTask.getType());
        assertEquals("CONFIRM w5hQjAvqPntVAdA4VSNw3LWZTLfUKO\r\n", connectTask.getMatcher().group(0));
        assertEquals("w5hQjAvqPntVAdA4VSNw3LWZTLfUKO", connectTask.getMatcher().group(1));
    }

    @Test
    void confirmInBadCase() {
        //Given
        Protocol protocol = new Protocol();
        String msgClientToServer = "CONFIRM zyvfu48894\r\n";
        //When
        Task connectTask = protocol.buildTask(msgClientToServer);
        //Then
        assertEquals(TaskType.MESSAGE, connectTask.getType());


        //When
         msgClientToServer = "CONFIRM YSrB7bhp9ksJUqp3ifKSm1Dozi2GKUQQ2fQCKrflajRD9CgQH6VyfzVTHgdFyorPYEcmXkwSOuHbvf44MJ4BgZ0TrnEIHj5Vo839YSrB7bhp9ksJUqp3ifKSm1Dozi2GKUQQ2fQCKrflajRD9CgQH6VyfzVTHgdFyorPYEcmXkwSOuHbvf44MJ4BgZ0TrnEIHj5Vo839YSrB7bhp9ksJUqp3ifKSm1Dozi2GKUQQ2fQCKrflajRD9CgQH6VyfzVTHgdFyorPYEcmXkwSOuHbvf44MJ4BgZ0TrnEIHj5Vo839YSrB7bhp9ksJUqp3ifKSm1Dozi2GKUQQ2fQCKrflajRD9CgQH6VyfzVTHgdFyorPYEcmXkwSOuHbvf44MJ4BgZ0TrnEIHj5Vo839\r\n";
         connectTask = protocol.buildTask(msgClientToServer);
        //Then
        assertEquals(TaskType.MESSAGE, connectTask.getType());

        //When
        msgClientToServer = "CONF jRD9CgQH6VyfzVTHgdFyojRD9CgQH6VyfzVTHgdFyorPYEcmXkwSOuHbvf44MJ4BgZ0TrnEIHj5Vo839\r\n";
        connectTask = protocol.buildTask(msgClientToServer);
        //Then
        assertEquals(TaskType.MESSAGE, connectTask.getType());
    }


    @Test
    void checkDisconnectInTheRightCase() {
        //Given
        Protocol protocol = new Protocol();
        String msgClientToServer = "DISCONNECT\r\n";
        //When
        Task connectTask = protocol.buildTask(msgClientToServer);
        //Then
        assertEquals(TaskType.DISCONNECT, connectTask.getType());
    }

    @Test
    void disconnectInBadCase() {
        //Given
        Protocol protocol = new Protocol();
        String msgClientToServer = "DISCO\r\n";
        //When
        Task connectTask = protocol.buildTask(msgClientToServer);
        //Then
        assertEquals(TaskType.MESSAGE, connectTask.getType());


        //When
         msgClientToServer = "DISCONNECT\n";
         connectTask = protocol.buildTask(msgClientToServer);
        //Then
        assertEquals(TaskType.MESSAGE, connectTask.getType());
    }
}
