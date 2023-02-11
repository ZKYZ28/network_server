package org.helmo.murmurG6.utils;

import org.helmo.murmurG6.models.User;

public class Challenger {

    public static String calculChallenge(User user, String random){
        String unHashedChallenge =
                random+
                "$2b$"+
                user.getBcryptRound()+
                "$"+
                user.getBcryptSalt()+
                user.getBcryptHash();

        return ShaUtils.sha3_256(unHashedChallenge);
    }
}
