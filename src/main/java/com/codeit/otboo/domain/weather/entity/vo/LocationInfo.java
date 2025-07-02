package com.codeit.otboo.domain.weather.entity.vo;

// 위치 정보 (JSON 매핑용)
public record LocationInfo(
        double latitude,
        double longitude,
        int x,
        int y
) {}