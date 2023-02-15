package org.helmo.murmurG6.models;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Protocol {

    /*
     * ACSII Characters
     * [\x21-\x2f] : correspond à la plage des codes hexadécimaux 0x21 à 0x2f, qui comprend les symboles suivants :
     * ! " # $ % & ' ( ) * + , - . /
     *
     * [\x3a-\x40] : correspond à la plage des codes hexadécimaux 0x3a à 0x40, qui comprend les symboles suivants :
     * : ; < = > ? @
     *
     * [\x5B-\x60] : correspond à la plage des codes hexadécimaux 0x5B à 0x60, qui comprend les symboles suivants :
     * [ \ ] ^ _ `
     *
     */

    private static final String RX_DIGIT = "[0-9]";
    private static final String RX_LETTER = "[a-zA-Z]";
    private static final String RX_LETTER_DIGIT = RX_LETTER + "|" + RX_DIGIT;
    private static final String RX_SYMBOL = "[\\x21-\\x2f]|[\\x3a-\\x40]|[\\x5B-\\x60]";
    private static final String RX_ROUND = "(" + RX_DIGIT + "{2})";
    private static final String RX_PASSCHAR = "[\\x22-\\xff]";
    private static final String RX_VISIBLE_CHARACTER = "[\\x20-\\xff]";
    private static final String RX_INFORMATION_MESSAGE = "((" + RX_VISIBLE_CHARACTER + "){0,200})";
    private static final String RX_RANDOM = "((" + RX_LETTER_DIGIT + "|" + RX_SYMBOL + "){22})";
    private static final String RX_BCRYPT_SALT = "((" + RX_LETTER_DIGIT + "|" + RX_SYMBOL + "){22})";
    private final static String RX_ESP = "\\s";
    private static final String RX_DOMAIN = "((" + RX_LETTER_DIGIT + "|\\.){5,200})";
    private static final String RX_USERNAME = "((" + RX_LETTER_DIGIT + "){5,20})";
    private static final String RX_USER_DOMAIN = "(" + RX_USERNAME + "@" + RX_DOMAIN + ")";
    private static final String RX_MESSAGE = "((" + RX_VISIBLE_CHARACTER + "){1,250})";
    private static final String RX_SHA3_EX = "((" + RX_LETTER_DIGIT + "){30,200})";

    /*PARTS*/
    private static final String RX_CRLF = "(\\x0d\\x0a){0,1}";
    private static final String RX_SALT_SIZE = "([0-9]{2})";
    private static final String RX_BCRYPT_HASH = "(\\$2b\\$\\d{2}\\$(" + RX_LETTER_DIGIT + "|" + RX_SYMBOL + "){1,70})";
    private static final String TAG = "#[a-zA-Z0-9]{5,20}";
    private static final String TAG_DOMAIN = "(" + TAG + "@" + RX_DOMAIN + ")";

    private static final String TAG_DOMAIN_OR_RX_USER_DOMAIN = "(" + RX_USER_DOMAIN + "|" + TAG_DOMAIN + ")";


    /*FULL*/
    private final static Pattern CONNECT = Pattern.compile("CONNECT" + RX_ESP + "(?<username>" + RX_USERNAME + ")" + RX_CRLF);
    private final static Pattern REGISTER = Pattern.compile("REGISTER" + RX_ESP + "(?<username>" + RX_USERNAME + ")" + RX_ESP + RX_SALT_SIZE + RX_ESP + "(?<bcrypt>" + RX_BCRYPT_HASH + ")" + RX_CRLF);
    private final static Pattern FOLLOW = Pattern.compile("FOLLOW" + RX_ESP + "(?<domain>" + TAG_DOMAIN_OR_RX_USER_DOMAIN + ")" + RX_CRLF);
    private final static Pattern CONFIRM = Pattern.compile("CONFIRM" + RX_ESP + "(?<challenge>" + RX_SHA3_EX + ")" + RX_CRLF);
    private final static Pattern DISCONNECT = Pattern.compile("DISCONNECT" + RX_CRLF);
    private final static Pattern MSG = Pattern.compile("MSG" + RX_ESP + "(?<message>" + RX_MESSAGE + ")" + RX_CRLF);

    private static final Map<Pattern, TaskType> TYPE_MESSAGE_MAP = Map.of(
            CONNECT, TaskType.CONNECT,
            REGISTER, TaskType.REGISTER,
            FOLLOW, TaskType.FOLLOW,
            CONFIRM, TaskType.CONFIRM,
            DISCONNECT, TaskType.DISCONNECT,
            MSG, TaskType.MSG
    );

    public static Task buildTask(String command) throws InvalidTaskException {
        for (Map.Entry<Pattern, TaskType> entry : TYPE_MESSAGE_MAP.entrySet()) {
            Matcher matcher = entry.getKey().matcher(command);
            if (matcher.matches()) {
                return new Task(entry.getValue(), matcher);
            }
        }
        throw new InvalidTaskException("Tache invalide!");
    }
}