package org.helmo.murmurG6.infrastructure.dto;

import org.helmo.murmurG6.models.*;

import java.util.*;

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

    public static TrendLibraryDto toDto(TrendLibrary library) {
        TrendLibraryDto dto = new TrendLibraryDto();

        for (var entry : library.getTrendMap().keySet()) {
            dto.trendMap.put(entry, Mapper.userCredentialsListToDto(library.getTrendMap().get(entry)));
        }
        return dto;
    }

    public static TrendLibrary fromDto(TrendLibraryDto dto) {
        TrendLibrary library = new TrendLibrary();

        if (dto != null && dto.trendMap != null) {
            for (var entry : dto.trendMap.keySet()) {
                library.getTrendMap().put(entry, Mapper.dtoListTouserCredentials(dto.trendMap.get(entry)));
            }
        }
        return library;
    }



    public static Map<String, TreeMap<String, OffLineMessage>> offLineMessagesFromDto(Map<String, TreeMap<String, OffLineMessageDto>> offlineMessagesDto){

        Map<String, TreeMap<String, OffLineMessage>> offlineMessages = new HashMap<>();

        if(offlineMessagesDto != null){
            //On parcours chaque clé de la liste de messages hors-ligne (les user creditentials dto)
            for(String uc : offlineMessagesDto.keySet()){

                //On rempli la liste de messages hors ligne avec les userCreditentials
                offlineMessages.put(uc, new TreeMap<>());

                //Pour chaque userCreditentialsDto dans la liste de message dto, on ajoute les messages
                for(String messageId : offlineMessagesDto.get(uc).keySet()){
                    offlineMessages.get(uc).put(
                            messageId,
                            OffLineMessageDto.fromDto(offlineMessagesDto.get(uc).get(messageId))
                    );
                }
            }
        }
        return offlineMessages;
    }

    public static Map<String, HashMap<String, OffLineMessageDto>> offLineMessagesToDto(Map<String, TreeMap<String, OffLineMessage>> offlineMessages){

        Map<String, HashMap<String, OffLineMessageDto>> offlineMessagesDto = new HashMap<>();

        if(offlineMessages != null){
            for(String uc : offlineMessages.keySet()){

                offlineMessagesDto.put(uc, new HashMap<>());
                for(String messageId : offlineMessages.get(uc).keySet()){
                    offlineMessagesDto.get(uc).put(
                            messageId,
                            OffLineMessageDto.toDto(offlineMessages.get(uc).get(messageId))
                    );
                }
            }
        }
        return offlineMessagesDto;
    }
}