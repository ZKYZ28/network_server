package org.helmo.murmurG6.models;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;


public class OffLineMessage implements Comparable{

    private final LocalDateTime dateTime;
    private final String message;

    public OffLineMessage(LocalDateTime dateTime, String message){
        this.dateTime = dateTime;
        this.message = message;
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
            return (int) (Math.abs(this.dateTime.toEpochSecond(ZoneOffset.UTC)) - Math.abs(otherMessage.dateTime.toEpochSecond(ZoneOffset.UTC)));
        } else {
            return 0;
        }
    }
}
