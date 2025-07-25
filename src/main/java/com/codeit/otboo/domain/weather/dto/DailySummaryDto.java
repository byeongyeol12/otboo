package com.codeit.otboo.domain.weather.dto;

import com.codeit.otboo.domain.weather.entity.vo.LocationInfo;
import com.codeit.otboo.domain.weather.entity.vo.PrecipitationInfo;
import com.codeit.otboo.domain.weather.entity.vo.SkyStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Builder
@Schema(description = "하루 날씨 요약 응답 DTO")
public class DailySummaryDto {

    @Schema(description = "UUID (그룹 대표)", example = "bdfbbd62-9787-4a36-997c-3e387b20217e")
    private final UUID id;

    @Schema(description = "예보 발표 시각(UTC ISO8601)", example = "2024-07-25T05:00:00Z")
    private final Instant forecastedAt;

    @Schema(description = "예보 대상 날짜", example = "2024-07-26")
    private final LocalDate forecastAt;

    @Schema(description = "위치 정보")
    private final LocationInfo location;

    @Schema(description = "하늘 상태(예: CLEAR, CLOUDY, ...)", example = "CLEAR")
    private final SkyStatus skyStatus;

    @Schema(description = "하루 평균 강수 확률(%)", example = "30.5")
    private final double precipitationProbability;

    @Schema(description = "평균 습도(%)", example = "68.0")
    private final double humidityCurrent;

    @Schema(description = "전일 대비 습도 차이(%)", example = "-2.5")
    private final double humidityComparedToDayBefore;

    @Schema(description = "평균 기온(℃)", example = "23.1")
    private final double tempCurrent;

    @Schema(description = "전일 대비 기온 차이(℃)", example = "0.8")
    private final double tempComparedToDayBefore;

    @Schema(description = "최저 기온(℃, 없으면 null)", example = "18.7")
    private final Double tempMin;

    @Schema(description = "최고 기온(℃, 없으면 null)", example = "29.2")
    private final Double tempMax;

    @Schema(description = "평균 풍속(m/s)", example = "3.2")
    private final double windSpeed;

    @Schema(description = "평균 풍속(문자)", example = "약간 강함")
    private final String windSpeedAsWord;
}
