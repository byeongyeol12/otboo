package com.codeit.otboo.domain.recommendation.dto;

import java.util.List;
import java.util.UUID;

public record RecommendationResponse(
	UUID weatherId,
	UUID userId,
	List<RecommendedClothesDto> clothes
) {
}
