package com.codeit.otboo.domain.recommendation.service;

import com.codeit.otboo.domain.clothes.dto.response.ClothesAttributeWithDefDto;
import com.codeit.otboo.domain.clothes.entity.Clothes;
import com.codeit.otboo.domain.clothes.entity.ClothesType;
import com.codeit.otboo.domain.clothes.repository.ClothesRepository;
import com.codeit.otboo.domain.recommendation.dto.RecommendationResponse;
import com.codeit.otboo.domain.recommendation.dto.RecommendedClothesDto;
import com.codeit.otboo.domain.user.entity.Profile;
import com.codeit.otboo.domain.user.entity.User;
import com.codeit.otboo.domain.user.repository.UserRepository;
import com.codeit.otboo.domain.weather.entity.Weather;
import com.codeit.otboo.domain.weather.repository.WeatherRepository;
import com.codeit.otboo.exception.CustomException;
import com.codeit.otboo.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecommendationService {

	private final ClothesRepository clothesRepository;
	private final WeatherRepository weatherRepository;
	private final UserRepository userRepository;

	public RecommendationResponse getRecommendations(UUID userId, UUID weatherId) {

		// 1. 사용자 정보를 먼저 조회합니다.
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

		// 2. 프론트에서 받은 weatherId로 날씨를 찾아봅니다.
		Optional<Weather> optionalWeather = weatherRepository.findById(weatherId);

		Weather weather;
		if (optionalWeather.isPresent()) {
			// 3-1. DB에 날씨 정보가 있으면, 그걸 사용합니다.
			weather = optionalWeather.get();
		} else {
			// 3-2. DB에 날씨 정보가 없으면 (프론트가 오래된 ID를 보낸 경우),
			//      에러를 내는 대신! 사용자 위치를 기반으로 최신 날씨를 다시 찾아옵니다.
			Profile profile = Optional.ofNullable(user.getProfile())
					.orElseThrow(() -> new CustomException(ErrorCode.PROFILE_NOT_FOUND));

			Integer x = profile.getX();
			Integer y = profile.getY();

			if (x == null || y == null) {
				throw new CustomException(ErrorCode.LOCATION_NOT_SET);
			}

			weather = weatherRepository.findLatestForecastByLocation(x, y, OffsetDateTime.now())
					.orElseThrow(() -> new CustomException(ErrorCode.WEATHER_NOT_FOUND_FOR_LOCATION));
		}

		List<Clothes> allUserClothes = clothesRepository.findAllByOwnerId(userId);

		List<Clothes> recommendedClothesList = recommend(allUserClothes, weather);

		List<RecommendedClothesDto> recommendedClothesDtos = recommendedClothesList.stream()
				.map(this::convertToRecommendedClothesDto)
				.collect(Collectors.toList());

		// 최종적으로 사용된 weather 객체의 ID를 응답에 담아 반환
		return new RecommendationResponse(weather.getId(), userId, recommendedClothesDtos);
	}

	// recommend 메서드에서 User 파라미터는 사용되지 않으므로 제거
	private List<Clothes> recommend(List<Clothes> allClothes, Weather weather) {

		double currentTemp = weather.getTemperature().current();

		Map<ClothesType, List<Clothes>> clothesByType = new EnumMap<>(ClothesType.class);
		for (Clothes c : allClothes) {
			clothesByType.computeIfAbsent(c.getType(), k -> new ArrayList<>()).add(c);
		}

		// 상의(TOP), 하의(BOTTOM), 아우터(OUTER) 순으로 추천하기 위해 Set 대신 List 사용
		List<ClothesType> typesToRecommend = new ArrayList<>(List.of(ClothesType.TOP, ClothesType.BOTTOM));

		// 온도가 22도 미만일 때만 아우터를 추천 목록에 추가
		if (currentTemp < 22.0) {
			typesToRecommend.add(ClothesType.OUTER);
		}

		List<Clothes> recommendation = new ArrayList<>();
		Random random = new Random();

		for (ClothesType type : typesToRecommend) {
			if (clothesByType.containsKey(type)) {
				List<Clothes> clothesList = clothesByType.get(type);
				if (!clothesList.isEmpty()) {
					Clothes randomClothes = clothesList.get(random.nextInt(clothesList.size()));
					recommendation.add(randomClothes);
				}
			}
		}

		return recommendation;
	}

	private RecommendedClothesDto convertToRecommendedClothesDto(Clothes clothes) {
		List<ClothesAttributeWithDefDto> attributeDtos = clothes.getAttributes().stream()
				.map(attribute -> new ClothesAttributeWithDefDto(
						attribute.getAttributeDef().getId(),
						attribute.getAttributeDef().getName(),
						Optional.ofNullable(attribute.getAttributeDef().getSelectableValues()).orElse(Collections.emptyList()),
						attribute.getValue()))
				.collect(Collectors.toList());

		return new RecommendedClothesDto(
				clothes.getId(),
				clothes.getName(),
				clothes.getImageUrl(),
				clothes.getType().name(),
				attributeDtos);
	}
}