package org.helmo.murmurG6.models;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;


public class OffLineMessage implements Comparable{

    private final String senderCreditentialsInString;
    private final LocalDateTime dateTime;
    private final String message;

    public OffLineMessage(String senderCreditentialsInString, LocalDateTime dateTime, String message){
        this.senderCreditentialsInString = senderCreditentialsInString;
        this.dateTime = dateTime;
        this.message = message;
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



    @Override
    public int compareTo(Object o) {
        if (o instanceof OffLineMessage) {
            OffLineMessage otherMessage = (OffLineMessage) o;

            int dateComparison = (int) (Math.abs(this.dateTime.toEpochSecond(ZoneOffset.UTC)) - Math.abs(otherMessage.dateTime.toEpochSecond(ZoneOffset.UTC)));
            if (dateComparison != 0) {
                return dateComparison;
            }

            int senderComparison = this.senderCreditentialsInString.compareTo(otherMessage.senderCreditentialsInString);
            if (senderComparison != 0) {
                return senderComparison;
            }

            return this.message.compareTo(otherMessage.message);
        }else {
            return 0;
        }
    }


}
