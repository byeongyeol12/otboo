package com.codeit.otboo.domain.weather.controller;

import com.codeit.otboo.domain.weather.dto.WeatherDto;
import com.codeit.otboo.domain.weather.entity.vo.LocationInfo;
import com.codeit.otboo.domain.weather.service.WeatherService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @RestController: 이 클래스가 RESTful 웹 서비스의 컨트롤러임을 나타냅니다.
 * 메소드의 반환값은 자동으로 JSON으로 변환됩니다.
 * @RequestMapping: 이 컨트롤러의 모든 메소드에 대한 기본 URL 경로를 설정합니다.
 */
@RestController
@RequestMapping("/api/weathers")
@RequiredArgsConstructor
public class WeatherController {

    private final WeatherService weatherService;

    /**
     * @GetMapping: HTTP GET 요청을 이 메소드에 매핑합니다.
     * URL: /api/weathers
     * @param latitude  URL 쿼리 파라미터 (?latitude=값)
     * @param longitude URL 쿼리 파라미터 (&longitude=값)
     * @return 날씨 정보 DTO 리스트를 포함하는 HTTP 응답
     */
    @GetMapping
    public ResponseEntity<List<WeatherDto>> getWeather(
            @RequestParam double latitude,
            @RequestParam double longitude) {

        List<WeatherDto> weatherData = weatherService.getWeather(latitude, longitude);
        return ResponseEntity.ok(weatherData); // HTTP 200 OK 상태와 함께 데이터를 반환
    }

    /**
     * @GetMapping("/location"): HTTP GET 요청을 이 메소드에 매핑합니다.
     * URL: /api/weathers/location
     */
    @GetMapping("/location")
    public ResponseEntity<LocationInfo> getWeatherLocation(
            @RequestParam double latitude,
            @RequestParam double longitude) {

        LocationInfo locationData = weatherService.getWeatherLocation(latitude, longitude);
        return ResponseEntity.ok(locationData);
    }
}