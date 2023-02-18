package org.helmo.murmurG6.infrastructure.dto;

import org.helmo.murmurG6.models.BCrypt;
import org.helmo.murmurG6.models.Trend;
import org.helmo.murmurG6.models.User;
import org.helmo.murmurG6.models.UserCredentials;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Mapper {

    public static BCrypt bcryptDtoToBcrypt(BCryptDto dto) {
        return BCryptDto.fromDto(dto);
    }

    public static BCryptDto bcryptToBcryptDto(BCrypt bcrypt) {
        return BCryptDto.toDto(bcrypt);
    }

    public static User userDtoToUser(UserDto dto) {
        return UserDto.fromDto(dto);
    }

    public static UserDto userToUserDto(User user) {
        return UserDto.toDto(user);
    }


    public static List<User> dtoToUserList(Iterable<UserDto> dtos) {
        List<User> users = new ArrayList<>();

        if (dtos != null) {
            for (UserDto dto : dtos) {
                users.add(Mapper.userDtoToUser(dto));
            }
        }
        return users;
    }

    public static List<UserDto> userListFromDto(Iterable<User> users) {
        List<UserDto> dtos = new ArrayList<>();

        if (users != null) {
            for (User user : users) {
                dtos.add(Mapper.userToUserDto(user));
            }
        }
        return dtos;
    }

    public static Set<UserCredentials> dtoListTouserCredentials(Iterable<UserCredentialsDto> dtos) {
        Set<UserCredentials> users = new HashSet<>();

        if (dtos != null) {
            for (UserCredentialsDto user : dtos) {
                users.add(UserCredentialsDto.fromDto(user));
            }
        }
        return users;
    }

    public static Set<UserCredentialsDto> userCredentialsListToDto(Iterable<UserCredentials> users) {
        Set<UserCredentialsDto> dtos = new HashSet<>();

        if (users != null) {
            for (UserCredentials user : users) {
                dtos.add(UserCredentialsDto.toDto(user));
            }
        }
        return dtos;
    }

    public static Set<Trend> trendsDtosToTrend(Iterable<TrendDto> dtos) {
        Set<Trend> users = new HashSet<>();

        if (dtos != null) {
            for (TrendDto trendDto : dtos) {
                users.add(TrendDto.fromDto(trendDto));
            }
        }
        return users;
    }

    public static Set<TrendDto> trendsToDto(Iterable<Trend> trends) {
        Set<TrendDto> dtos = new HashSet<>();

        if (trends != null) {
            for (Trend trend : trends) {
                dtos.add(TrendDto.toDto(trend));
            }
        }
        return dtos;
    }
}