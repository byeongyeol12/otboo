package com.codeit.otboo.domain.user.mapper;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.codeit.otboo.domain.user.dto.request.UserCreateRequest;
import com.codeit.otboo.domain.user.dto.response.LocationDto;
import com.codeit.otboo.domain.user.dto.response.ProfileDto;
import com.codeit.otboo.domain.user.entity.Profile;

@Mapper(componentModel = "spring")
public interface ProfileMapper {
	Profile toEntity(UserCreateRequest request);

	@Mapping(source = "user.id", target = "userId")
	@Mapping(source = "user.name", target = "name")
	@Mapping(source = "locationNames", target = "location")
	@Mapping(source = "profile.profileImageUrl", target = "profileImageUrl")
	ProfileDto toDto(Profile profile);

	// 커스텀 매핑: locationName(String) → LocationDto
	default LocationDto map(String locationName) {
		if (locationName == null || locationName.isBlank()) {
			return new LocationDto(null, null, null, null, List.of());
		}

		// 예시 파싱 로직: 쉼표로 나뉘어 있다고 가정
		List<String> locationNames = Arrays.asList(locationName.split(","));
		return new LocationDto(null, null, null, null, locationNames);
	}

	// 커스텀 매핑: Instant → LocalDate
	default LocalDate map(Instant birthDate) {
		return birthDate == null ? null : birthDate.atZone(ZoneId.systemDefault()).toLocalDate();
	}
}