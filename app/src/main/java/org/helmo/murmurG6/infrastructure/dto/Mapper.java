package org.helmo.murmurG6.infrastructure.dto;

import org.helmo.murmurG6.models.BCrypt;
import org.helmo.murmurG6.models.User;
import java.util.ArrayList;
import java.util.List;

public class Mapper {

    public static BCrypt bcryptDtoToBcrypt(BCryptDto dto) { return BCryptDto.fromDtoToBcrypt(dto);}
    public static BCryptDto bcryptToBcryptDto(BCrypt bcrypt) { return BCryptDto.fromBcryptToDto(bcrypt);}
    public static User userDtoToUser(UserDto dto) { return UserDto.fromUserDtoToUser(dto); }
    public static UserDto userToUserDto(User user) { return UserDto.fromUserToDto(user); }


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
