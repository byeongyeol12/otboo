package com.codeit.otboo.domain.weather.controller;

import com.codeit.otboo.domain.weather.dto.WeatherDto;
import com.codeit.otboo.domain.weather.service.WeatherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "날씨", description = "날씨 정보 조회 API")
@RestController
@RequestMapping("/api/weathers")
@RequiredArgsConstructor
public class WeatherController {

    private final WeatherService weatherService;

    @Operation(summary = "일별 날씨 예보 조회", description = "특정 좌표의 일별 날씨 예보 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping
    public ResponseEntity<List<WeatherDto>> getWeather(
            @Parameter(description = "위도", example = "37.790564") @RequestParam double latitude,
            @Parameter(description = "경도", example = "127.0741998") @RequestParam double longitude) {

        List<WeatherDto> dailySummary = weatherService.getWeather(latitude, longitude);
        return ResponseEntity.ok(dailySummary);
    }
}