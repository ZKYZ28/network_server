package org.helmo.murmurG6.infrastructure.dto;

import org.helmo.murmurG6.models.OffLineMessage;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class OffLineMessageDto implements Comparable{

    public String dateTime;
    public String message;

    public static OffLineMessage fromDto(OffLineMessageDto dto) {
        return new OffLineMessage(LocalDateTime.parse(dto.dateTime, DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")), dto.message);
    }

    public static OffLineMessageDto toDto(OffLineMessage offLineMessage) {
        OffLineMessageDto dto = new OffLineMessageDto();
        dto.dateTime = offLineMessage.getDateTime();
        dto.message = offLineMessage.getMessage();
        return dto;
    }


    @Override
    public int compareTo(Object o) {
        if (o instanceof OffLineMessageDto) {
            OffLineMessageDto otherMessage = (OffLineMessageDto) o;
            return dateTime.compareTo(otherMessage.dateTime);
        } else {
            return 0;
        }
    }
}
