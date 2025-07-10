package com.codeit.otboo.domain.weather.dto;

import java.util.UUID;

import com.codeit.otboo.domain.weather.entity.vo.PrecipitationInfo;
import com.codeit.otboo.domain.weather.entity.vo.SkyStatus;
import com.codeit.otboo.domain.weather.entity.vo.TemperatureInfo;

public record WeatherForFeedDto(
	UUID weatherId,
	SkyStatus skyStatus,
	PrecipitationInfo precipitation,
	TemperatureInfo temperature

	) {
}
