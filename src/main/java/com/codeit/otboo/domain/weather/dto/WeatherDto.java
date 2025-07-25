package com.codeit.otboo.domain.weather.dto;

import com.codeit.otboo.domain.weather.entity.vo.*;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "날씨 정보 DTO")
public record WeatherDto(
        @Schema(description = "날씨 데이터 ID", example = "bdfbbd62-9787-4a36-997c-3e387b20217e")
        UUID id,

        @Schema(description = "예보 발표 시각(UTC ISO8601)", example = "2024-07-25T05:00:00Z")
        Instant forecastedAt,

        @Schema(description = "예보 대상 시각(UTC ISO8601)", example = "2024-07-25T18:00:00Z")
        Instant forecastAt,

        @Schema(description = "위치 정보")
        LocationInfo location,

        @Schema(description = "하늘 상태 (예: CLEAR, CLOUDY, ...)", example = "CLEAR")
        SkyStatus skyStatus,

        @Schema(description = "강수 정보")
        PrecipitationInfo precipitation,

        @Schema(description = "습도 정보")
        HumidityInfo humidity,

        @Schema(description = "온도 정보")
        TemperatureInfo temperature,

        @Schema(description = "풍속 정보")
        WindSpeedInfo windSpeed,

        @Schema(description = "강수 형태 (예: NONE, RAIN, SNOW 등)", example = "RAIN")
        PrecipitationType precipitationType
) {}
