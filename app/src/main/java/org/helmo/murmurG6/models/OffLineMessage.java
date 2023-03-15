package org.helmo.murmurG6.models;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;


public class OffLineMessage{

    private String messageId;
    private final String senderCreditentialsInString;
    private final LocalDateTime dateTime;
    private String message;

    public OffLineMessage(String senderCreditentialsInString, LocalDateTime dateTime, String message){
        this.senderCreditentialsInString = senderCreditentialsInString;
        this.dateTime = dateTime;
        this.message = message;
    }


    public String getMessageId(){
        return this.messageId;
    }
    public String getSenderCreditentialsInString() {
        return senderCreditentialsInString;
    }
    public String getDateTime() {
        return dateTime.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));
    }
    public String getMessage() {
        return message;
    }


    public void setEncryptedMessage(String ciphertext_base64) {
        this.message = ciphertext_base64;
        this.messageId = (int) dateTime.toEpochSecond(ZoneOffset.UTC)+senderCreditentialsInString;
    }
}
