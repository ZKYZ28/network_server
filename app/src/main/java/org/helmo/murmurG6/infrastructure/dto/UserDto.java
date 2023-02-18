package org.helmo.murmurG6.infrastructure.dto;

import org.helmo.murmurG6.models.BCrypt;
import org.helmo.murmurG6.models.Trend;
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

    public String login;
    public BCryptDto bcryptDto;
    public Set<UserCredentialsDto> followedUsers;
    public Set<TrendDto> followedTrends;


    public static User fromDto(UserDto dto) {
        return new User(dto.login, Mapper.bcryptDtoToBcrypt(dto.bcryptDto), Mapper.dtoListTouserCredentials(dto.followedUsers), Mapper.trendsDtosToTrend(dto.followedTrends));
    }

    public static UserDto toDto(User user) {
        UserDto dto = new UserDto();
        dto.login = user.getLogin();
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

class UserCredentialsDto {

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

class TrendDto {

    public String trendName;
    public String domain;

    public static Trend fromDto(TrendDto dto) {
        return new Trend(dto.trendName, dto.domain);
    }

    public static TrendDto toDto(Trend trend) {
        TrendDto dto = new TrendDto();
        dto.trendName = trend.getTrendName();
        dto.domain = trend.getDomain();
        return dto;
    }
}

