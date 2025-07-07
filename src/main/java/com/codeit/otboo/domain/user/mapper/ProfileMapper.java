package com.codeit.otboo.domain.user.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.codeit.otboo.domain.user.dto.request.ProfileUpdateRequest;
import com.codeit.otboo.domain.user.dto.request.UserCreateRequest;
import com.codeit.otboo.domain.user.dto.response.ProfileDto;
import com.codeit.otboo.domain.user.entity.Profile;

@Mapper(componentModel = "spring")
public interface ProfileMapper {
	Profile toEntity(UserCreateRequest request);

	@Mapping(source = "user.id", target = "userId")
	@Mapping(source = "user.name", target = "name")
	@Mapping(source = "locationNames", target = "location")
	ProfileDto toDto(Profile profile);

	void updateProfileFromRequest(ProfileUpdateRequest request, @MappingTarget Profile profile);
}