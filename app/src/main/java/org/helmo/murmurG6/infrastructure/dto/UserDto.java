package org.helmo.murmurG6.infrastructure.dto;

import org.helmo.murmurG6.models.FollowInformation;
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
    public List<FollowInformation> followedUsers; //liste des loggins des users followed
    public List<FollowInformation> followedTrends; //liste des hashtags

    public static User fromUserDtoToUser(UserDto dto) {
        User user =  new User(dto.login, Mapper.bcryptDtoToBcrypt(dto.bcryptDto));
        user.setFollowedUsers(dto.followedUsers);
        user.setFollowedTrends(dto.followedTrends);
        return user;
    }

    public static UserDto fromUserToDto(User user) {
        UserDto dto = new UserDto();
        dto.login = user.getLogin();
        dto.bcryptDto = Mapper.bcryptToBcryptDto(user.getBcrypt());
        dto.followedUsers = user.getFollowedUsers();
        dto.followedTrends = user.getFollowedTrends();
        return dto;
    }
}
