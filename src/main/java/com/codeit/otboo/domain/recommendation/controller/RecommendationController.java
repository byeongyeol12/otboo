package com.codeit.otboo.domain.recommendation.controller;

import com.codeit.otboo.domain.recommendation.dto.RecommendationResponse;
import com.codeit.otboo.domain.recommendation.service.RecommendationService;
import com.codeit.otboo.global.config.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "의상 추천", description = "날씨 기반 의상 추천 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/recommendations")
public class RecommendationController {

	private final RecommendationService recommendationService;

	@Operation(summary = "오늘의 의상 추천받기", description = "사용자 프로필과 현재(또는 지정된) 날씨를 기반으로 적합한 의상을 추천합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "추천 성공"),
			@ApiResponse(responseCode = "404", description = "사용자 또는 날씨 정보를 찾을 수 없음")
	})
	@GetMapping
	public ResponseEntity<RecommendationResponse> getRecommendations(
			@Parameter(description = "특정 날씨 기준으로 추천받고 싶을 때 사용하는 날씨 ID") @RequestParam(name = "weatherId", required = false) UUID weatherId,
			@Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal userPrincipal) {

		UUID userId = userPrincipal.getId();
		RecommendationResponse response = recommendationService.getRecommendations(userId, weatherId);
		return ResponseEntity.ok(response);
	}
}