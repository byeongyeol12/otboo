package com.codeit.otboo.domain.user.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import com.codeit.otboo.domain.user.dto.request.ProfileUpdateRequest;
import com.codeit.otboo.domain.user.dto.request.UserCreateRequest;
import com.codeit.otboo.domain.user.dto.response.ProfileDto;
import com.codeit.otboo.domain.user.entity.Profile;

@Mapper(componentModel = "spring")
public interface ProfileMapper {

	/*@Mapping(target = "id", expression = "java(UUID.randomUUID())")
	@Mapping(target = "user", ignore = true)*/
	Profile toEntity(UserCreateRequest request);

	ProfileDto toDto(Profile profile);

	void updateProfileFromRequest(ProfileUpdateRequest request, @MappingTarget Profile profile);
}