package com.codeit.otboo.domain.location;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LocationService {

	private final KakaoLocalService kakaoLocalService;
	private final GridCoordinateConverter gridCoordinateConverter;

	public LocationReseponse getLocationInfo(double latitude, double longitude) {
		KakaoRegionDto region = kakaoLocalService.getRegionFromCoords(latitude, longitude);
		GridCoordinate grid = gridCoordinateConverter.toGrid(latitude, longitude);
		return new LocationReseponse(
			latitude,
			longitude,
			grid.getX(),
			grid.getY(),
			List.of(region.region1(), region.region2(), region.region3(), "")
		);
	}
}
