package org.helmo.murmurG6.models;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Protocol {

    /*PARTS*/
    private final static String RX_ESP = " ";
    private final static String RX_CRLF = "(\\r\\n)?";
    private final static String RX_USERNAME = "(?i)(^[a-z])((?![-]$)[a-z-Ã§]){0,24}$";
    private final static String RX_DOMAIN = "^((?!-)[A-Za-z0-9-]{1,63}(?<!-)\\\\.)+[A-Za-z]{2,6}$";
    private static final String RX_SALT_SIZE = "[0-9]{2}";
    private static final String RX_BCRYPT_HASH = "^\\$2[ayb]\\$.{56}$";
    private static final String TAG_DOMAIN = "#[a-zA-Z0-9]{5,20}";


    /*FULL*/
    private final static String CONNECT = "CONNECT" + RX_ESP + RX_USERNAME + RX_CRLF;
    private final String REGISTER = "REGISTER" + RX_ESP + RX_USERNAME + RX_ESP + RX_SALT_SIZE + RX_ESP + RX_BCRYPT_HASH + RX_CRLF;
    private final String FOLLOW = "FOLLOW" + RX_ESP + RX_DOMAIN + TAG_DOMAIN + RX_CRLF;
    private String TYPE_MESSAGE[] = {CONNECT, REGISTER, FOLLOW};

    public Message analyseMessage(String msg){
        for (int i = 0; i < TYPE_MESSAGE.length; i++) {
            if(Pattern.matches(TYPE_MESSAGE[i], msg)){
                return new Message(identifyTypeMessage(i), createMatcher(msg, i), msg);
            }
        }

        return new Message(MessageType.MESSAGE, null, msg);
    }
    public MessageType identifyTypeMessage(int i){
        switch (i){
            case 0:
                return MessageType.CONNECT;
            case 1:
                return MessageType.REGISTER;
            case 2:
                return MessageType.FOLLOW;
            default:
                return MessageType.MESSAGE;
        }
    }

    public Matcher createMatcher(String msg, int i){
        Pattern pattern = Pattern.compile(TYPE_MESSAGE[i]);
        return pattern.matcher(msg);
    }

    public static void main(String[] args) {
       System.out.println(Pattern.matches("", ""));
    }
}




