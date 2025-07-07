package com.codeit.otboo.domain.user.component;

import com.codeit.otboo.domain.user.dto.response.KakaoAddressResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Objects;

@Slf4j
@Component
public class KakaoApiClient {

    private final WebClient webClient;
    private final String apiKey;

    public KakaoApiClient(@Value("${API_KAKAO_REST_KEY}") String apiKey) {
        this.apiKey = "KakaoAK " + apiKey; // "KakaoAK " 접두사를 붙여야 합니다.
        this.webClient = WebClient.builder()
                .baseUrl("https://dapi.kakao.com")
                .defaultHeader(HttpHeaders.AUTHORIZATION, this.apiKey)
                .build();
    }

    public List<String> fetchLocationNames(double latitude, double longitude) {
        try {
            KakaoAddressResponse response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/v2/local/geo/coord2address.json")
                            .queryParam("x", String.valueOf(longitude)) // 카카오는 x가 경도(longitude)
                            .queryParam("y", String.valueOf(latitude))  // y가 위도(latitude)
                            .build())
                    .retrieve()
                    .bodyToMono(KakaoAddressResponse.class)
                    .block();

            // 응답이 비어있지 않은지 확인
            if (response != null && !response.getDocuments().isEmpty()) {
                KakaoAddressResponse.Address address = response.getDocuments().get(0).getAddress();
                // "경기도", "양주시", "옥정동" 과 같은 리스트를 반환
                return List.of(address.getRegion1(), address.getRegion2(), address.getRegion3());
            }
        } catch (Exception e) {
            log.error("Failed to fetch location names from Kakao API", e);
        }
        // 실패 시 비어있는 리스트 반환
        return List.of();
    }
}