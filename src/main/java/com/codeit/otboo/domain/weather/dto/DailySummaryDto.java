package com.codeit.otboo.domain.weather.dto;

import com.codeit.otboo.domain.weather.entity.vo.LocationInfo;
import com.codeit.otboo.domain.weather.entity.vo.PrecipitationInfo;
import com.codeit.otboo.domain.weather.entity.vo.SkyStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Builder
public class DailySummaryDto {
    private final UUID id;                       // 그룹을 대표할 랜덤 UUID
    private final Instant forecastedAt;          // 예보 발표 시각 (가장 최신)
    private final LocalDate forecastAt;          // 예보 대상 날짜
    private final LocationInfo location;
    private final SkyStatus skyStatus;           // 단순히 첫 번째 항목의 skyStatus 사용
    private final double precipitationProbability; // 하루 평균 강수 확률
    // 습도: 평균과 전일 대비 차이
    private final double humidityCurrent;
    private final double humidityComparedToDayBefore;
    // 온도: 평균, 전일 대비 차이, 최저·최고
    private final double tempCurrent;
    private final double tempComparedToDayBefore;
    private final Double tempMin;
    private final Double tempMax;
    // 풍속: 평균 속도와 asWord
    private final double windSpeed;
    private final String windSpeedAsWord;
}