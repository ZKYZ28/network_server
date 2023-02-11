package org.helmo.murmurG6.infrastructure.dto;

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


    /*public UserDto(String login, BCryptDto bcryptDto, List<String> followedUsers, List<String> followedTrends) {
        this.login = login;
        this.bcryptDto = bcryptDto;
        this.followedUsers = followedUsers;
        this.followedTrends = followedTrends;
    }*/


}
