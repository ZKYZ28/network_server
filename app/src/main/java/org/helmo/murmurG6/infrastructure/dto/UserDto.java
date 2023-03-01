package org.helmo.murmurG6.infrastructure.dto;

import org.helmo.murmurG6.models.User;

import java.util.Set;

/**
 * /**
 * La classe UserDto représente un objet DTO (Data Transfer Object) pour les utilisateurs de l'application.
 * Il permet de transférer les données de l'utilisateur entre les couches d'infrastructure et de modèle. (Dans ce cas ci en Json)
 *
 * @version 1.0
 * @since 11 février 2023
 */
public class UserDto {

    public UserCredentialsDto login;
    public BCryptDto bcryptDto;
    public Set<UserCredentialsDto> followers;
    public Set<TrendDto> followedTrends;


    public static User fromDto(UserDto dto) {
        return new User(UserCredentialsDto.fromDto(dto.login), Mapper.bcryptDtoToBcrypt(dto.bcryptDto), Mapper.dtoListTouserCredentials(dto.followers), Mapper.trendsDtosToTrend(dto.followedTrends));
    }

    public static UserDto toDto(User user) {
        UserDto dto = new UserDto();

        dto.login = UserCredentialsDto.toDto(user.getCredentials());
        dto.bcryptDto = Mapper.bcryptToBcryptDto(user.getBcrypt());
        dto.followers = Mapper.userCredentialsListToDto(user.getUserFollowers());
        dto.followedTrends = Mapper.trendsToDto(user.getFollowedTrends());
        return dto;
    }
}

