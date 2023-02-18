package org.helmo.murmurG6.infrastructure.dto;

import org.helmo.murmurG6.models.Trend;

public class TrendDto {

    public String trendName;
    public String domain;

    public static Trend fromDto(TrendDto dto) {
        return new Trend(dto.trendName, dto.domain);
    }

    public static TrendDto toDto(Trend trend) {
        TrendDto dto = new TrendDto();
        dto.trendName = trend.getTrendName();
        dto.domain = trend.getDomain();
        return dto;
    }
}

