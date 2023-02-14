package org.helmo.murmurG6.infrastructure.dto;

import org.helmo.murmurG6.models.FollowInformation;

public class FollowInformationDto {

    public String informationFollow;

    public String serverDomain;

    public static FollowInformationDto fromFollowInformationToFollowInformationDto(FollowInformation followInformation) {
        FollowInformationDto followInformationDto = new FollowInformationDto();
        followInformationDto.informationFollow = followInformation.getInformationFollow();
        followInformationDto.serverDomain = followInformation.getServerDomain();
        return followInformationDto;
    }

    public static FollowInformation fromFollowInformationDtoToFollowInformation(FollowInformationDto followInformationDto) {
        return new FollowInformation(followInformationDto.informationFollow, followInformationDto.serverDomain);
    }
}
