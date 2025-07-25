package com.codeit.otboo.domain.recommendation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.UUID;

@Schema(description = "날씨 기반 추천 의상 응답 DTO")
public record RecommendationResponse(

		@Schema(description = "날씨 정보 ID", example = "bdfbbd62-9787-4a36-997c-3e387b20217e")
		UUID weatherId,

		@Schema(description = "추천 대상 사용자 ID", example = "a8efc13e-9a11-41fa-bf68-994de9e29c7a")
		UUID userId,

		@Schema(description = "추천 의상 리스트")
		List<RecommendedClothesDto> clothes

) { }
