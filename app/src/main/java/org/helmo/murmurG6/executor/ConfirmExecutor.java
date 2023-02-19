package org.helmo.murmurG6.executor;

import org.helmo.murmurG6.controller.ClientRunnable;
import org.helmo.murmurG6.controller.Protocol;
import org.helmo.murmurG6.controller.ServerController;
import org.helmo.murmurG6.models.User;

import java.util.regex.Matcher;

public class ConfirmExecutor {

    protected static void confirm(ClientRunnable client, String challengeReceived){
        User user = client.getUser();
        String expected = user.getBcrypt().generateChallenge(client.getRandom22());
        client.sendMessage(controlConfirm(challengeReceived, expected));
    }

    private static String controlConfirm(String clientChallenge, String userChallenge) {
        return clientChallenge.equals(userChallenge) ? Protocol.build_OK() : Protocol.build_ERROR();
    }
}
