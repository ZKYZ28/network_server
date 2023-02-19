package org.helmo.murmurG6.infrastructure.dto;

import org.helmo.murmurG6.models.UserCredentials;

public class UserCredentialsDto {

    public String login;
    public String domain;

    public static UserCredentials fromDto(UserCredentialsDto dto) {
        return new UserCredentials(dto.login, dto.domain);
    }

    public static UserCredentialsDto toDto(UserCredentials user) {
        UserCredentialsDto dto = new UserCredentialsDto();
        dto.login = user.getLogin();
        dto.domain = user.getDomain();
        return dto;
    }
}
