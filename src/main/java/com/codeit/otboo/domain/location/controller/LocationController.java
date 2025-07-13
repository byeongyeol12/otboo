package com.codeit.otboo.domain.location.controller;

import com.codeit.otboo.domain.location.dto.response.LocationResponse;
import com.codeit.otboo.domain.location.service.LocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/weathers") // 참고: 팀의 API 명세에 따라 '/api/locations' 등으로 변경될 수 있습니다.
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;

    @GetMapping("/location")
    public ResponseEntity<LocationResponse> getLocation(
            @RequestParam double latitude,
            @RequestParam double longitude
    ) {
        LocationResponse location = locationService.getLocationInfo(latitude, longitude);
        return ResponseEntity.ok(location);
    }
}