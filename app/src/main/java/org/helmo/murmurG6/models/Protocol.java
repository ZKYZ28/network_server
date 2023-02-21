package org.helmo.murmurG6.models;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Protocol {

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
    public static final Pattern RX_USER_DOMAIN = Pattern.compile("(" + "(?<login>" + RX_USERNAME + ")" + "@" + "(?<userServerDomain>" + RX_DOMAIN + "))");
    private static final String RX_MESSAGE = "((" + RX_VISIBLE_CHARACTER + "){1,250})";
    private static final String RX_SHA3_EX = "((" + RX_LETTER_DIGIT + "){30,200})";
    private static final String RX_ID_DOMAIN = "((" + RX_DIGIT + "){1,5})" + "@" + RX_DOMAIN;

    /*PARTS*/
    private static final String RX_CRLF = "(\\x0d\\x0a){0,1}";
    private static final String RX_SALT_SIZE = "([0-9]{2})";
    private static final String RX_BCRYPT_HASH = "(\\$2b\\$\\d{2}\\$(" + RX_LETTER_DIGIT + "|" + RX_SYMBOL + "){1,70})";
    public static final String TAG = "#[a-zA-Z0-9]{5,20}";
    public static final Pattern TAG_DOMAIN = Pattern.compile("(" + "(?<tagName>" + TAG + ")" + "@" + "(?<trendServerDomain>" + RX_DOMAIN + "))");

    public static final Pattern TAG_DOMAIN_OR_RX_USER_DOMAIN = Pattern.compile("(" + RX_USER_DOMAIN + "|" + TAG_DOMAIN + ")");


    /*FULL*/
    private final static Pattern RX_CONNECT_TASK = Pattern.compile("CONNECT" + RX_ESP + "(?<username>" + RX_USERNAME + ")" + RX_CRLF);
    private final static Pattern RX_REGISTER_TASK = Pattern.compile("REGISTER" + RX_ESP + "(?<username>" + RX_USERNAME + ")" + RX_ESP + RX_SALT_SIZE + RX_ESP + "(?<bcrypt>" + RX_BCRYPT_HASH + ")" + RX_CRLF);
    private final static Pattern RX_FOLLOW_TASK = Pattern.compile("FOLLOW" + RX_ESP + "(?<domain>" + TAG_DOMAIN_OR_RX_USER_DOMAIN + ")" + RX_CRLF);
    private final static Pattern RX_CONFIRM_TASK = Pattern.compile("CONFIRM" + RX_ESP + "(?<challenge>" + RX_SHA3_EX + ")" + RX_CRLF);
    private final static Pattern RX_DISCONNECT_TASK = Pattern.compile("DISCONNECT" + RX_CRLF);
    private final static Pattern RX_MSG_TASK = Pattern.compile("MSG" + RX_ESP + "(?<message>" + RX_MESSAGE + ")" + RX_CRLF);

    //"MSGS" esp nom_domaine esp message crlf
    private final static Pattern RX_MSGS = Pattern.compile("MSGS" + RX_ESP + RX_USER_DOMAIN + RX_ESP + RX_MESSAGE + RX_CRLF);



//************************************************************************************************************************
    private static final String SEND_RX_USER_DOMAIN = "(" + RX_USERNAME + "@" + RX_DOMAIN +")";
    private static final String SEND_TAG_DOMAIN = "(" + TAG + "@" + RX_DOMAIN + ")";
    private static final String SEND_TAG_DOMAIN_OR_RX_USER_DOMAIN = "(" + SEND_RX_USER_DOMAIN + "|" + SEND_TAG_DOMAIN + ")";
    private static final String SEND_RX_FOLLOW_TASK = "FOLLOW" + RX_ESP + SEND_TAG_DOMAIN_OR_RX_USER_DOMAIN + RX_CRLF;
    private static final String SEND_RX_MSGS = "MSGS" + RX_ESP + SEND_RX_USER_DOMAIN + RX_ESP + RX_MESSAGE + RX_CRLF;
    public final static Pattern RX_SEND = Pattern.compile("SEND" + RX_ESP + "(?<id>" + RX_ID_DOMAIN + ")" + RX_ESP +
            "(?<sender>" + SEND_RX_USER_DOMAIN + ")" + RX_ESP +
            "(?<receiver>" +SEND_TAG_DOMAIN_OR_RX_USER_DOMAIN + ")" + RX_ESP +
            "(?<content>" + SEND_RX_FOLLOW_TASK + "|" +SEND_RX_MSGS + ")" + RX_CRLF);
//************************************************************************************************************************


    private final static String MSGS = "MSGS <message>\r\n";
    private final static String FOLLOW = "FOLLOW <itemToFollow>\r\n";
    private final static String HELLO = "HELLO <ip> <salt>\r\n";
    private final static String PARAM = "PARAM <round> <salt>\r\n";
    private final static String SEND = "SEND <id> <nom_domaine> <nom/tag_domain> <message_interne>\r\n";
    private final static String ERROR = "-ERR\r\n";
    private final static String OK = "+OK\r\n";


    public static String build_MSGS(String msg) {
        return MSGS.replace("<message>", msg);
    }

    public static String build_FOLLOW(String itemToFollow) {
        return FOLLOW.replace("<itemToFollow>", itemToFollow);
    }

    public static String build_HELLO(String ip, String salt) {
        return HELLO.replace("<ip>", ip).replace("<salt>", salt);
    }

    public static String build_PARAM(int round, String salt) {
        return PARAM.replace("<round>", String.valueOf(round)).replace("<salt>", salt);
    }

    public static String build_SEND(String id, String nom_domain, String nomOrTag_domain, String message_interne) {
        return SEND.replace("<id>", id).replace("<nom_domaine>", nom_domain).replace("<nom/tag_domain>", nomOrTag_domain).replace("<message_interne>", message_interne);
    }

    public static String build_ERROR() {
        return ERROR;
    }

    public static String build_OK() {
        return OK;
    }


    private static final Map<TaskType, Pattern> TYPE_MESSAGE_MAP = Map.of(
            TaskType.CONNECT, RX_CONNECT_TASK,
            TaskType.REGISTER, RX_REGISTER_TASK,
            TaskType.FOLLOW, RX_FOLLOW_TASK,
            TaskType.CONFIRM, RX_CONFIRM_TASK,
            TaskType.DISCONNECT, RX_DISCONNECT_TASK,
            TaskType.MSG, RX_MSG_TASK,
            TaskType.MSGS, RX_MSGS
    );

    public static Matcher getMatcher(TaskType type, String command){
        Matcher matcher = TYPE_MESSAGE_MAP.get(type).matcher(command);
        if(matcher.matches()){
            return matcher;
        }
        return null;
    }

    public static TaskType detectTaskType(String command){
        for(TaskType type: TYPE_MESSAGE_MAP.keySet()){
            if(Pattern.matches(String.valueOf(TYPE_MESSAGE_MAP.get(type)), command)){
                return type;
            }
        }
        return TaskType.UNKNOWN;
    }

}