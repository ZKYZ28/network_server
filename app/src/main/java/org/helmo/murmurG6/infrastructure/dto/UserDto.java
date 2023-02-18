package org.helmo.murmurG6.infrastructure.dto;

import org.helmo.murmurG6.models.BCrypt;
import org.helmo.murmurG6.models.User;
import org.helmo.murmurG6.models.UserCredentials;

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

    public UserCredentials login;
    public BCryptDto bcryptDto;
    public Set<UserCredentialsDto> followedUsers;
    public Set<TrendDto> followedTrends;


    public static User fromDto(UserDto dto) {
        return new User(dto.login, Mapper.bcryptDtoToBcrypt(dto.bcryptDto), Mapper.dtoListTouserCredentials(dto.followedUsers), Mapper.trendsDtosToTrend(dto.followedTrends));
    }

    public static UserDto toDto(User user) {
        UserDto dto = new UserDto();

        dto.login = user.getCredentials();
        dto.bcryptDto = Mapper.bcryptToBcryptDto(user.getBcrypt());
        dto.followedUsers = Mapper.userCredentialsListToDto(user.getUserFollowers());
        dto.followedTrends = Mapper.trendsToDto(user.getFollowedTrends());
        return dto;
    }
}

class BCryptDto {
    public int rounds;
    public String salt;
    public String hash;

    public static BCrypt fromDto(org.helmo.murmurG6.infrastructure.dto.BCryptDto dto) {
        return new BCrypt(dto.rounds, dto.salt, dto.hash);
    }

    public static org.helmo.murmurG6.infrastructure.dto.BCryptDto toDto(BCrypt bcrypt) {
        org.helmo.murmurG6.infrastructure.dto.BCryptDto bc = new org.helmo.murmurG6.infrastructure.dto.BCryptDto();
        bc.salt = bcrypt.getSalt();
        bc.rounds = bcrypt.getRounds();
        bc.hash = bcrypt.getHash();
        return bc;
    }
}

