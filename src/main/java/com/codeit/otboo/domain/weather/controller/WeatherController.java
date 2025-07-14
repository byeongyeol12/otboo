package com.codeit.otboo.domain.weather.controller;

import com.codeit.otboo.domain.weather.dto.WeatherDto;
import com.codeit.otboo.domain.weather.service.WeatherService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/weathers")
@RequiredArgsConstructor
public class WeatherController {

    private final WeatherService weatherService;

    /**
     * 기존에 시간별 리스트를 반환하던 getWeather()를
     * 내부에서 ‘일별 요약’으로 바꿔치기 합니다.
     */
    @GetMapping
    public ResponseEntity<List<WeatherDto>> getWeather(
            @RequestParam double latitude,
            @RequestParam double longitude) {

        List<WeatherDto> dailySummary = weatherService.getWeather(latitude, longitude);
        return ResponseEntity.ok(dailySummary);
    }
}
