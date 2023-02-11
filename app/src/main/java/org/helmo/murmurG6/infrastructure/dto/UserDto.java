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
    public BcryptHashDto hashParts;
    public List<String> followedUsers; //liste des loggins des users followed
    public List<String> followedTrends; //liste des hashtags


    /**
     * Converti un objet userDto en un objet User
     *
     * @param dto un userDto
     * @return un User
     */
    public static User userDtoToUser(UserDto dto){
        User result = new User(dto.login, BcryptHashDto.hashPartsDtoToHashParts(dto.hashParts));
        result.setFollowedUsers(dto.followedUsers);
        result.setFollowedTrends(dto.followedTrends);

        return result;
    }

}
