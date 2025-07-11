package com.codeit.otboo.domain.location;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/weathers")
@RequiredArgsConstructor
public class LocationController {

	private final LocationService locationService;

	@GetMapping("/location")
	public ResponseEntity<LocationReseponse> getLocation(
		@RequestParam double latitude,
		@RequestParam double longitude
	) {
		LocationReseponse location = locationService.getLocationInfo(latitude, longitude);
		return ResponseEntity.ok(location);
	}
}
