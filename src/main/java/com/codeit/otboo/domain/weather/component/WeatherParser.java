package com.codeit.otboo.domain.weather.component;

import com.codeit.otboo.domain.weather.dto.KmaWeatherResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class WeatherParser {

    private final ObjectMapper objectMapper;

    /**
     * KMA API 응답 문자열을 파싱하여 예보 Item 리스트를 반환합니다.
     * @param jsonString 원본 JSON 응답
     * @return 예보 Item 리스트
     */
    public List<KmaWeatherResponse.Item> parse(String jsonString) {
        try {
            KmaWeatherResponse response = objectMapper.readValue(jsonString, KmaWeatherResponse.class);

            if (response == null || response.getResponse() == null || response.getResponse().getBody() == null
                    || response.getResponse().getBody().getItems() == null) {
                log.warn("KMA API response body or items are null. Raw Response: {}", jsonString);
                return Collections.emptyList();
            }
            return response.getResponse().getBody().getItems().getItemList();

        } catch (JsonProcessingException e) {
            log.error("Failed to parse KMA API response. Raw Response: {}", jsonString, e);
            return Collections.emptyList();
        }
    }
}