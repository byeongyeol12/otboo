package com.codeit.otboo.domain.recommendation.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.codeit.otboo.domain.clothes.dto.response.ClothesAttributeWithDefDto;
import com.codeit.otboo.domain.clothes.entity.Clothes;
import com.codeit.otboo.domain.clothes.entity.ClothesType;
import com.codeit.otboo.domain.clothes.repository.ClothesRepository;
import com.codeit.otboo.domain.recommendation.dto.RecommendationResponse;
import com.codeit.otboo.domain.recommendation.dto.RecommendedClothesDto;
import com.codeit.otboo.domain.user.entity.User;
import com.codeit.otboo.domain.user.repository.UserRepository;
import com.codeit.otboo.domain.weather.entity.Weather;
import com.codeit.otboo.domain.weather.entity.vo.HumidityInfo;
import com.codeit.otboo.domain.weather.entity.vo.LocationInfo;
import com.codeit.otboo.domain.weather.entity.vo.PrecipitationInfo;
import com.codeit.otboo.domain.weather.entity.vo.PrecipitationType;
import com.codeit.otboo.domain.weather.entity.vo.SkyStatus;
import com.codeit.otboo.domain.weather.entity.vo.TemperatureInfo;
import com.codeit.otboo.domain.weather.entity.vo.WindSpeedInfo;
import com.codeit.otboo.domain.weather.repository.WeatherRepository;
import com.codeit.otboo.exception.CustomException;
import com.codeit.otboo.global.error.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecommendationService {

	private final ClothesRepository clothesRepository;
	private final WeatherRepository weatherRepository;
	private final UserRepository userRepository;

	public RecommendationResponse getRecommendations(UUID userId, UUID weatherId) {

		Optional<Weather> optionalWeather = weatherRepository.findById(weatherId);

		Weather weather;
		if (optionalWeather.isPresent()) {
			weather = optionalWeather.get();
		} else {
			// 임시 데이터
			weather = Weather.builder()
				.temperature(new TemperatureInfo(15.0, 15.0, 15.0, 0.0)) // 임시로 15도
				.skyStatus(SkyStatus.CLEAR) // 임시 값
				.location(new LocationInfo(0.0, 0.0, 0, 0)) // 임시 값 (위도, 경도, x, y)
				.forecastedAt(Instant.now()) // 현재 시각으로 임시 설정
				.forecastAt(Instant.now().plusSeconds(3600)) // 1시간 후로 임시 설정
				.humidity(new HumidityInfo(0, 0)) // 임시 값
				.precipitation(new PrecipitationInfo(PrecipitationType.NONE, 0.0, 0.0)) // <-- 이 부분 수정
				.windSpeed(new WindSpeedInfo(0.0, "잔잔함")) // <-- 이 부분 수정
				.build();
		}

		User user = userRepository.findById(userId)
			.orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
		List<Clothes> allUserClothes = clothesRepository.findAllByOwnerId(userId);

		List<Clothes> recommendedClothesList = recommend(allUserClothes, weather, user);

		List<RecommendedClothesDto> recommendedClothesDtos = recommendedClothesList.stream()
			.map(this::convertToRecommendedClothesDto)
			.collect(Collectors.toList());

		return new RecommendationResponse(weatherId, userId, recommendedClothesDtos);
	}

	private List<Clothes> recommend(List<Clothes> allClothes, Weather weather, User user) {
		// 온도별 의상 추천
		double currentTemp = weather.getTemperature().current();

		Map<ClothesType, List<Clothes>> clothesByType = new EnumMap<>(ClothesType.class);
		for (Clothes c : allClothes) {
			clothesByType.computeIfAbsent(c.getType(), k -> new ArrayList<>()).add(c);
		}

		List<ClothesType> requiredTypes = new ArrayList<>();
		if (currentTemp >= 22.0) {
			requiredTypes.add(ClothesType.TOP);
			requiredTypes.add(ClothesType.BOTTOM);
		} else {
			requiredTypes.add(ClothesType.TOP);
			requiredTypes.add(ClothesType.BOTTOM);
			requiredTypes.add(ClothesType.OUTER);
		}

		int maxSets = requiredTypes.stream()
			.map(type -> clothesByType.getOrDefault(type, Collections.emptyList()).size())
			.min(Integer::compare)
			.orElse(0);

		int setsToRecommend = Math.min(3, maxSets);

		List<Clothes> recommendation = new ArrayList<>();

		for (int i = 0; i < setsToRecommend; i++) {
			for (ClothesType type : requiredTypes) {
				if (clothesByType.containsKey(type) && i < clothesByType.get(type).size()) {
					recommendation.add(clothesByType.get(type).get(i));
				}
			}
		}
		return recommendation;
	}

	private RecommendedClothesDto convertToRecommendedClothesDto(Clothes clothes) {
		List<ClothesAttributeWithDefDto> attributeDtos = clothes.getAttributes().stream()
			.map(attribute -> {
				List<String> selectableValues = attribute.getAttributeDef().getSelectableValues();
				return new ClothesAttributeWithDefDto(
					attribute.getAttributeDef().getId(),
					attribute.getAttributeDef().getName(),
					selectableValues != null ? selectableValues : Collections.emptyList(),
					attribute.getValue());
			})
			.collect(Collectors.toList());

		return new RecommendedClothesDto(
			clothes.getId(),
			clothes.getName(),
			clothes.getImageUrl(),
			clothes.getType().name(),
			attributeDtos);
	}
}