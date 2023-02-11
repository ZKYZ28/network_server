package org.helmo.murmurG6.infrastructure.dto;

import org.helmo.murmurG6.models.User;

import java.util.ArrayList;
import java.util.List;

public class UserDto {
    public String login;
    public String bcryptHash;
    public int bcryptRound;
    public String bcryptSalt;
    public List<String> followedUsers;
    public List<String> followedTrends;

    public static User userDtoToUser(UserDto dto){
        User result = new User(dto.login, dto.bcryptHash, dto.bcryptRound, dto.bcryptSalt);
        result.setFollowedUsers(dto.followedUsers);
        result.setFollowedTrends(dto.followedTrends);

        return result;
    }
}
