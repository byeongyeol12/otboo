package com.codeit.otboo.domain.location;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.codeit.otboo.exception.CustomException;
import com.codeit.otboo.global.error.ErrorCode;

@Service
public class KakaoLocalService {

	private final WebClient webClient;

	public KakaoLocalService(@Value("${kakao.rest-api-key}") String kakaoKey) {
		this.webClient = WebClient.builder()
			.baseUrl("https://dapi.kakao.com")
			.defaultHeader("Authorization", "KakaoAK " + kakaoKey)
			.build();
	}

	public KakaoRegionDto getRegionFromCoords(double latitude, double longitude) {
		return webClient.get()
			.uri(uriBuilder -> uriBuilder
				.path("/v2/local/geo/coord2regioncode.json")
				.queryParam("x", longitude)
				.queryParam("y", latitude)
				.build())
			.retrieve()
			.bodyToMono(KakaoRegionResponse.class)
			.blockOptional()
			.map(KakaoRegionResponse::toDto)
			.orElseThrow(() -> new CustomException(ErrorCode.LOCATION_NOT_FOUND));
	}
}
