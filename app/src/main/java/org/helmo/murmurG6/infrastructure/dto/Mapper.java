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
        BCryptDto bc = new BCryptDto();
        bc.salt = bcrypt.getSalt();
        bc.rounds = bcrypt.getRounds();
        bc.hash = bcrypt.getHash();
        return bc;
    }

    public static User userDtoToUser(UserDto dto){
        User result = new User(dto.login, Mapper.bcryptDtoToBcrypt(dto.bcryptDto));
        result.setFollowedUsers(dto.followedUsers);
        result.setFollowedTrends(dto.followedTrends);

        return result;
    }

    public static UserDto userToUserDto(User user){
        UserDto dto = new UserDto();
        dto.login = user.getLogin();
        dto.bcryptDto = new BCryptDto();
        dto.bcryptDto.rounds = user.getBcryptRound();
        dto.bcryptDto.salt = user.getBcryptSalt();
        dto.bcryptDto.hash = user.getBcryptHash();
        dto.followedUsers = user.getFollowedUsers();
        dto.followedTrends = user.getFollowedTrends();
        return dto;
        //return new UserDto(user.getLogin(), Mapper.bcryptToBcryptDto(user.getBcrypt()), user.getFollowedUsers(), user.getFollowedTrends());
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
