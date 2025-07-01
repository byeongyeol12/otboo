package com.codeit.otboo.domain.weather.entity.vo;

// 강수 정보 (JSON 매핑용)
public record PrecipitationInfo(
        PrecipitationType type,
        double amount,
        double probability
) {}