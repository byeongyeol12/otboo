package com.codeit.otboo.domain.weather.dto;

import com.codeit.otboo.domain.weather.entity.vo.*; // vo import
import java.time.OffsetDateTime;
import java.util.UUID;

// Weather Entity를 API 응답 스펙에 맞게 변환한 DTO
public record WeatherDto(
        UUID id,
        OffsetDateTime forecastedAt,
        OffsetDateTime forecastAt,
        LocationInfo location, // LocationInfo는 vo에 있으므로 재사용
        SkyStatus skyStatus,
        PrecipitationInfo precipitation,
        HumidityInfo humidity,
        TemperatureInfo temperature,
        WindSpeedInfo windSpeed
) {}