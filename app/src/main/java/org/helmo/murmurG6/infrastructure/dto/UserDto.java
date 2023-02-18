package org.helmo.murmurG6.infrastructure.dto;

import org.helmo.murmurG6.models.User;
import java.util.List;

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
    public List<String> followedUsers; //liste des loggins des users followed
    public List<String> followedTrends; //liste des hashtags

    public static User fromUserDtoToUser(UserDto dto) {
        return new User(dto.login, Mapper.bcryptDtoToBcrypt(dto.bcryptDto), dto.followedUsers, dto.followedTrends);
    }

    public static UserDto fromUserToDto(User user) {
        UserDto dto = new UserDto();
        dto.login = user.getLogin();
        dto.bcryptDto = Mapper.bcryptToBcryptDto(user.getBcrypt());
        dto.followedUsers = user.getUserFollowers();
        dto.followedTrends = user.getFollowedTrends();
        return dto;
    }
}
