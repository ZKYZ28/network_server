package org.helmo.murmurG6.infrastructure.dto;

import org.helmo.murmurG6.models.OffLineMessage;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class OffLineMessageDto{

    public String senderCreditentialsInString;
    public String dateTime;
    public String message;

    public static OffLineMessage fromDto(OffLineMessageDto dto) {
        return new OffLineMessage(dto.senderCreditentialsInString, LocalDateTime.parse(dto.dateTime, DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")), dto.message);
    }

    public static OffLineMessageDto toDto(OffLineMessage offLineMessage) {
        OffLineMessageDto dto = new OffLineMessageDto();
        dto.senderCreditentialsInString = offLineMessage.getSenderCreditentialsInString();
        dto.dateTime = offLineMessage.getDateTime();
        dto.message = offLineMessage.getMessage();
        return dto;
    }
}
