package org.helmo.murmurG6.infrastructure.dto;

import org.helmo.murmurG6.models.BCrypt;
import org.helmo.murmurG6.models.User;

import java.util.ArrayList;
import java.util.List;

public class Mapper {
    public static BCrypt bcryptDtoToBcrypt(BCryptDto dto){
        return new BCrypt(dto.rounds, dto.salt, dto.hash);
    }

    public static BCryptDto bcryptToBcryptDto(BCrypt bcrypt){
        return new BCryptDto(bcrypt.getRounds(), bcrypt.getSalt(), bcrypt.getHash());
    }

    public static User userDtoToUser(UserDto dto){
        User result = new User(dto.login, Mapper.bcryptDtoToBcrypt(dto.bcryptDto));
        result.setFollowedUsers(dto.followedUsers);
        result.setFollowedTrends(dto.followedTrends);

        return result;
    }

    public static UserDto userToUserDto(User user){
        return new UserDto(user.getLogin(), Mapper.bcryptToBcryptDto(user.getBcrypt()), user.getFollowedUsers(), user.getFollowedTrends());
    }

    public static List<User> userListFromDto(Iterable<UserDto> dtos) {
        List<User> users = new ArrayList<>();

        if (dtos != null) {
            for (UserDto dto : dtos) {
                users.add(Mapper.userDtoToUser(dto));
            }
        }

        return users;
    }

    public static Iterable<UserDto> userDtoListFromUsers(Iterable<User> users) {
        List<UserDto> dtos = new ArrayList<>();

        if (users != null) {
            for (User user : users) {
                dtos.add(Mapper.userToUserDto(user));
            }
        }
        return dtos;
    }
}
