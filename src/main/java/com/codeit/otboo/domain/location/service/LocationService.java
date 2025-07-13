package com.codeit.otboo.domain.location.service;

import com.codeit.otboo.domain.location.api.KakaoLocalApiService;
import com.codeit.otboo.domain.location.dto.KakaoRegionDto;
import com.codeit.otboo.domain.location.dto.response.LocationResponse;
import com.codeit.otboo.domain.location.vo.GridCoordinate;
import com.codeit.otboo.domain.location.util.GridCoordinateConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LocationService {

    private final KakaoLocalApiService kakaoLocalApiService;
    private final GridCoordinateConverter gridCoordinateConverter;

    public LocationResponse getLocationInfo(double latitude, double longitude) {
        KakaoRegionDto region = kakaoLocalApiService.getRegionFromCoords(latitude, longitude);
        GridCoordinate grid = gridCoordinateConverter.toGrid(latitude, longitude);

        return new LocationResponse(
                latitude,
                longitude,
                grid.getX(),
                grid.getY(),
                List.of(region.region1(), region.region2(), region.region3())
        );
    }
}
