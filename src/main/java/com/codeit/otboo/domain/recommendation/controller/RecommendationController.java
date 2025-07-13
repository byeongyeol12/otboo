// RecommendationController.java 파일

package com.codeit.otboo.domain.recommendation.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.codeit.otboo.domain.recommendation.dto.RecommendationResponse;
import com.codeit.otboo.domain.recommendation.service.RecommendationService;
import com.codeit.otboo.global.config.security.UserPrincipal;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/recommendations")
public class RecommendationController {

	private final RecommendationService recommendationService;

	@GetMapping
	public ResponseEntity<RecommendationResponse> getRecommendations(
			@RequestParam(name = "weatherId", required = false) UUID weatherId,
			@AuthenticationPrincipal UserPrincipal userPrincipal) {

		UUID userId = userPrincipal.getId();
		RecommendationResponse response = recommendationService.getRecommendations(userId, weatherId);
		return ResponseEntity.ok(response);
	}
}