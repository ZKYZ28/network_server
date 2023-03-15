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
            return dateTime.compareTo(otherMessage.dateTime);
        } else {
            return 0;
        }
    }


    /**
     * On compare 2 messages offline avec leur contenu et leur date (si 2 meme contenu de difference de date < 5 seconde => egal
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OffLineMessage that = (OffLineMessage) o;
        if (Math.abs(this.dateTime.toEpochSecond(ZoneOffset.UTC) - that.dateTime.toEpochSecond(ZoneOffset.UTC)) > 5) {
            return false;
        }
        return this.message.equals(that.message);
    }

}
