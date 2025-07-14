package com.codeit.otboo.domain.weather.component;

import com.codeit.otboo.domain.weather.dto.KmaWeatherResponse;
import com.codeit.otboo.domain.weather.dto.WeatherDto;
import com.codeit.otboo.domain.weather.entity.vo.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class WeatherParser {

    private final ObjectMapper objectMapper;

    public List<WeatherDto> parseAndGroup(String jsonString, double latitude, double longitude) {
        try {
            KmaWeatherResponse response = objectMapper.readValue(jsonString, KmaWeatherResponse.class);

            if (response == null || response.getResponse() == null || response.getResponse().getBody() == null
                    || response.getResponse().getBody().getItems() == null) {
                log.warn("KMA API response body or items are null. Raw Response: {}", jsonString);
                return Collections.emptyList();
            }

            Map<String, List<KmaWeatherResponse.Item>> groupedByDateTime =
                    response.getResponse().getBody().getItems().getItemList().stream()
                            .collect(Collectors.groupingBy(item -> item.getFcstDate() + item.getFcstTime()));

            return groupedByDateTime.values().stream()
                    .map(group -> createWeatherDtoFromGroup(group, latitude, longitude))
                    .collect(Collectors.toList());

        } catch (JsonProcessingException e) {
            log.error("Failed to parse KMA API response. The response is likely not JSON. Raw Response: {}", jsonString);
            return Collections.emptyList();
        }
    }

    private WeatherDto createWeatherDtoFromGroup(List<KmaWeatherResponse.Item> items, double latitude, double longitude) {
        Map<String, String> categoryValueMap = items.stream()
                .collect(Collectors.toMap(KmaWeatherResponse.Item::getCategory, KmaWeatherResponse.Item::getFcstValue, (v1, v2) -> v1));

        KmaWeatherResponse.Item firstItem = items.get(0);

        LocationInfo location = new LocationInfo(latitude, longitude, firstItem.getNx(), firstItem.getNy());

        TemperatureInfo temperature = new TemperatureInfo(
                parseDouble(categoryValueMap.get("TMP")),
                parseDouble(categoryValueMap.get("TMN")),
                parseDouble(categoryValueMap.get("TMX")),
                0.0
        );

        PrecipitationInfo precipitation = new PrecipitationInfo(
                mapToPrecipitationType(categoryValueMap.getOrDefault("PTY", "0")),
                parseDouble(categoryValueMap.get("PCP")),
                parseDouble(categoryValueMap.get("POP"))
        );

        HumidityInfo humidity = new HumidityInfo(
                parseDouble(categoryValueMap.get("REH")),
                0.0
        );

        WindSpeedInfo windSpeed = new WindSpeedInfo(
                parseDouble(categoryValueMap.get("WSD")),
                mapToWindSpeedAsWord(parseDouble(categoryValueMap.get("WSD")))
        );

        SkyStatus skyStatus = mapToSkyStatus(categoryValueMap.getOrDefault("SKY", "1"));

        return new WeatherDto(
                UUID.randomUUID(),
                parseDateTime(firstItem.getBaseDate(), firstItem.getBaseTime()),
                parseDateTime(firstItem.getFcstDate(), firstItem.getFcstTime()),
                location,
                skyStatus,
                precipitation,
                humidity,
                temperature,
                windSpeed,
                precipitation.type()
        );
    }

    private double parseDouble(String value) {
        if (value == null || !value.matches("[-+]?\\d*\\.?\\d+([eE][-+]?\\d+)?")) {
            return 0.0;
        }
        return Double.parseDouble(value);
    }

    /**
     * 날짜와 시간 문자열을 받아, 한국 시간(KST) 기준으로 해석한 뒤
     * 세계 표준시(UTC)인 Instant 타입으로 변환합니다.
     */
    private Instant parseDateTime(String date, String time) {
        LocalDate localDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyyMMdd"));
        LocalTime localTime = LocalTime.parse(time, DateTimeFormatter.ofPattern("HHmm"));
        return OffsetDateTime.of(localDate, localTime, ZoneOffset.ofHours(9)).toInstant();
    }

    private SkyStatus mapToSkyStatus(String skyCode) {
        return switch (skyCode) {
            case "1" -> SkyStatus.CLEAR;
            case "3" -> SkyStatus.MOSTLY_CLOUDY;
            case "4" -> SkyStatus.CLOUDY;
            default -> SkyStatus.CLEAR;
        };
    }

    private PrecipitationType mapToPrecipitationType(String ptyCode) {
        return switch (ptyCode) {
            case "1" -> PrecipitationType.RAIN;
            case "2" -> PrecipitationType.RAIN_SNOW;
            case "3" -> PrecipitationType.SNOW;
            case "4" -> PrecipitationType.SHOWER;
            default -> PrecipitationType.NONE;
        };
    }

    private String mapToWindSpeedAsWord(double speed) {
        if (speed < 4) return "약함";
        if (speed < 9) return "약간 강함";
        if (speed < 14) return "강함";
        return "매우 강함";
    }
}
