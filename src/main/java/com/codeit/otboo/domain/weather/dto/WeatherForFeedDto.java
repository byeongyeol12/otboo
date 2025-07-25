package com.codeit.otboo.domain.weather.dto;

import com.codeit.otboo.domain.weather.entity.vo.PrecipitationInfo;
import com.codeit.otboo.domain.weather.entity.vo.SkyStatus;
import com.codeit.otboo.domain.weather.entity.vo.TemperatureInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "피드에 표시되는 간단 날씨 정보 DTO")
public record WeatherForFeedDto(

		@Schema(description = "날씨 정보 ID", example = "bdfbbd62-9787-4a36-997c-3e387b20217e")
		UUID weatherId,

		@Schema(description = "하늘 상태 (예: CLEAR, CLOUDY, ...)", example = "CLEAR")
		SkyStatus skyStatus,

		@Schema(description = "강수 정보")
		PrecipitationInfo precipitation,

		@Schema(description = "온도 정보")
		TemperatureInfo temperature

) { }
