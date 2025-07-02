package com.codeit.otboo.domain.weather.entity.vo;

// 풍속 정보 (JSON 매핑용)
public record WindSpeedInfo(
        double speed,
        String asWord
) {}