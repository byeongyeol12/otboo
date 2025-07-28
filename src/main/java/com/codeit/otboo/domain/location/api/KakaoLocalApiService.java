package com.codeit.otboo.domain.location.api;

import com.codeit.otboo.domain.location.dto.KakaoRegionDto;
import com.codeit.otboo.domain.location.dto.response.KakaoRegionResponse;
import com.codeit.otboo.exception.CustomException;
import com.codeit.otboo.global.error.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class KakaoLocalApiService {

    private final WebClient webClient;

    public KakaoLocalApiService(@Value("${API_KAKAO_REST_KEY}") String kakaoKey) {
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