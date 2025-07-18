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
	@Mapping(source = ".", target = "location") // profile 전체를 넘김
	@Mapping(source = "profileImageUrl", target = "profileImageUrl")
	ProfileDto toDto(Profile profile);

	// ↓ 아래 방식으로 커스텀 매핑
	default LocationDto map(Profile profile) {
		if (profile == null) return null;
		List<String> locationNames = profile.getLocationNames() == null ?
				List.of() :
				Arrays.stream(profile.getLocationNames().split(","))
						.map(String::trim)
						.toList();

		return new LocationDto(
				profile.getLatitude(),
				profile.getLongitude(),
				profile.getX(),
				profile.getY(),
				locationNames
		);
	}

	default LocalDate map(Instant birthDate) {
		return birthDate == null ? null : birthDate.atZone(ZoneId.systemDefault()).toLocalDate();
	}
}