package com.codeit.otboo.domain.weather.entity.vo;

// 습도 정보 (JSON 매핑용)
public record HumidityInfo(
        double current,
        double comparedToDayBefore
) {}