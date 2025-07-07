package com.codeit.otboo.domain.recommendation.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.codeit.otboo.domain.recommendation.dto.RecommendationResponse;
import com.codeit.otboo.domain.recommendation.service.RecommendationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/recommendations")
public class RecommendationController {

	private final RecommendationService recommendationService;

	@GetMapping
	public ResponseEntity<RecommendationResponse> getRecommendations(
		@RequestParam("weatherId") UUID weatherId,
		@RequestParam("userId") UUID userId) {

		RecommendationResponse response = recommendationService.getRecommendations(userId, weatherId);
		return ResponseEntity.ok(response);
	}

}
