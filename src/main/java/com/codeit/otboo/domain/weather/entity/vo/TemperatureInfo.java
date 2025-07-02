package com.codeit.otboo.domain.weather.entity.vo;

// 기온 정보 (JSON 매핑용)
public record TemperatureInfo(
        double current,
        double min,
        double max,
        double comparedToDayBefore
) {}