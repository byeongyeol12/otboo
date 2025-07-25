package com.codeit.otboo.domain.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "위치 정보 DTO")
public record LocationDto(
		@Schema(description = "위도", example = "37.790564")
		Double latitude,

		@Schema(description = "경도", example = "127.0741998")
		Double longitude,

		@Schema(description = "기상청 격자 X좌표", example = "61")
		Integer x,

		@Schema(description = "기상청 격자 Y좌표", example = "132")
		Integer y,

		@Schema(description = "위치 이름 목록", example = "[\"경기도\", \"양주시\", \"회천4동\"]")
		List<String> locationNames
) {
}