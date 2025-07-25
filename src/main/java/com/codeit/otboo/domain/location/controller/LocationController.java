package com.codeit.otboo.domain.location.controller;

import com.codeit.otboo.domain.location.dto.response.LocationResponse;
import com.codeit.otboo.domain.location.service.LocationService;
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

@Tag(name = "위치", description = "좌표 기반 위치 정보 변환 API")
@RestController
@RequestMapping("/api/weathers")
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;

    @Operation(summary = "좌표를 행정구역명으로 변환", description = "위도, 경도 좌표를 받아 기상청 격자 좌표 및 행정구역명으로 변환합니다.")
    @ApiResponse(responseCode = "200", description = "변환 성공")
    @GetMapping("/location")
    public ResponseEntity<LocationResponse> getLocation(
            @Parameter(description = "위도", required = true, example = "37.790564") @RequestParam double latitude,
            @Parameter(description = "경도", required = true, example = "127.0741998") @RequestParam double longitude
    ) {
        LocationResponse location = locationService.getLocationInfo(latitude, longitude);
        return ResponseEntity.ok(location);
    }
}