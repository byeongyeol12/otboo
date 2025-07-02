package com.codeit.otboo.domain.weather.component;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Component
@Slf4j
public class KmaApiClient {

    private final WebClient webClient;
    private final String baseUrl;
    private final String serviceKey;

    public KmaApiClient(
            @Value("${api.kma.base-url}") String baseUrl,
            @Value("${api.kma.service-key}") String serviceKey
    ) {
        this.baseUrl = baseUrl;
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .build();
        this.serviceKey = serviceKey;
    }

    /**
     * 기상청 단기예보 API를 호출하여 원본 응답 문자열을 반환합니다.
     * @param nx 예보지점 X 좌표
     * @param ny 예보지점 Y 좌표
     * @return 기상청 API 응답 결과 (JSON 문자열)
     */
    public String fetchWeatherForecast(int nx, int ny) {
        BaseDateTime bdt = calculateBaseDateTime(LocalDate.now(), LocalTime.now());

        String encodedKey;
        try {
            encodedKey = URLEncoder.encode(serviceKey, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            log.error("Failed to encode serviceKey", e);
            throw new RuntimeException("Service key encoding failed", e);
        }

        URI uri = UriComponentsBuilder
                .fromUriString(baseUrl)
                .path("/getVilageFcst")
                .queryParam("ServiceKey", encodedKey) // 파라미터 이름은 'ServiceKey' (대문자 S)
                .queryParam("pageNo", 1)
                .queryParam("numOfRows", 1000)
                .queryParam("dataType", "JSON")
                .queryParam("base_date", bdt.baseDate())
                .queryParam("base_time", bdt.baseTime())
                .queryParam("nx", nx)
                .queryParam("ny", ny)
                .build(true)
                .toUri();

        try {
            return webClient.get()
                    .uri(uri)
                    .header(HttpHeaders.USER_AGENT,
                            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/108.0.0.0 Safari/537.36")
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (WebClientResponseException e) {
            log.error("KMA API Error: status={}, body={}",
                    e.getRawStatusCode(), e.getResponseBodyAsString());
            throw e;
        }
    }

    /**
     * 현재 시각을 기준으로 기상청 API 요청에 필요한 base_date와 base_time을 계산합니다.
     */
    private BaseDateTime calculateBaseDateTime(LocalDate date, LocalTime time) {
        LocalTime baseTime;
        if (time.isBefore(LocalTime.of(2, 10))) {
            date = date.minusDays(1);
            baseTime = LocalTime.of(23, 0);
        } else if (time.isBefore(LocalTime.of(5, 10))) {
            baseTime = LocalTime.of(2, 0);
        } else if (time.isBefore(LocalTime.of(8, 10))) {
            baseTime = LocalTime.of(5, 0);
        } else if (time.isBefore(LocalTime.of(11, 10))) {
            baseTime = LocalTime.of(8, 0);
        } else if (time.isBefore(LocalTime.of(14, 10))) {
            baseTime = LocalTime.of(11, 0);
        } else if (time.isBefore(LocalTime.of(17, 10))) {
            baseTime = LocalTime.of(14, 0);
        } else if (time.isBefore(LocalTime.of(20, 10))) {
            baseTime = LocalTime.of(17, 0);
        } else if (time.isBefore(LocalTime.of(23, 10))) {
            baseTime = LocalTime.of(20, 0);
        } else {
            baseTime = LocalTime.of(23, 0);
        }

        String dateStr = date.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String timeStr = baseTime.format(DateTimeFormatter.ofPattern("HHmm"));
        return new BaseDateTime(dateStr, timeStr);
    }

    private record BaseDateTime(String baseDate, String baseTime) {}
}