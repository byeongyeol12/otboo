package com.codeit.otboo.domain.weather.dto;

import com.codeit.otboo.domain.weather.entity.vo.*;
import java.time.Instant;
import java.util.UUID;

public record WeatherDto(
        UUID id,
        Instant forecastedAt,
        Instant forecastAt,
        LocationInfo location,
        SkyStatus skyStatus,
        PrecipitationInfo precipitation,
        HumidityInfo humidity,
        TemperatureInfo temperature,
        WindSpeedInfo windSpeed
) {}
