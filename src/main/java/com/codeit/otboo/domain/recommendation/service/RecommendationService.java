package com.codeit.otboo.domain.recommendation.service;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.codeit.otboo.domain.clothes.dto.response.ClothesAttributeWithDefDto;
import com.codeit.otboo.domain.clothes.entity.Clothes;
import com.codeit.otboo.domain.clothes.repository.ClothesRepository;
import com.codeit.otboo.domain.recommendation.dto.RecommendationResponse;
import com.codeit.otboo.domain.recommendation.dto.RecommendedClothesDto;
import com.codeit.otboo.domain.user.entity.User;
import com.codeit.otboo.domain.user.repository.UserRepository;
import com.codeit.otboo.domain.weather.entity.Weather;
import com.codeit.otboo.domain.weather.repository.WeatherRepository;
import com.codeit.otboo.exception.CustomException;
import com.codeit.otboo.global.error.ErrorCode;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class RecommendationService {

	private final ClothesRepository clothesRepository;
	private final WeatherRepository weatherRepository;
	private final UserRepository userRepository;

	public RecommendationResponse getRecommendations(UUID userId, UUID weatherId) {

		List<Clothes> allUserClothes = clothesRepository.findAllByOwnerId(userId);

		List<Clothes> recommendedClothesList = allUserClothes;

		List<RecommendedClothesDto> recommendedClothesDtos = recommendedClothesList.stream()
			.map(this::convertToRecommendedClothesDto)
			.collect(Collectors.toList());
		
		return new RecommendationResponse(weatherId, userId, recommendedClothesDtos);
	}

	private RecommendedClothesDto convertToRecommendedClothesDto(Clothes clothes) {
		List<ClothesAttributeWithDefDto> attributeDtos = clothes.getAttributes().stream()
			.map(attribute -> {
				List<String> selectableValues = attribute.getAttributeDef().getSelectableValues();
				return new ClothesAttributeWithDefDto(
					attribute.getAttributeDef().getId(),
					attribute.getAttributeDef().getName(),
					selectableValues != null ? selectableValues : Collections.emptyList(),
					attribute.getValue()
				);
			})
			.collect(Collectors.toList());

		return new RecommendedClothesDto(
			clothes.getId(),
			clothes.getName(),
			clothes.getImageUrl(),
			clothes.getType().name(),
			attributeDtos
		);
	}
}
