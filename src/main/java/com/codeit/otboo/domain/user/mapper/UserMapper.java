package com.codeit.otboo.domain.user.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.codeit.otboo.domain.user.dto.request.UserCreateRequest;
import com.codeit.otboo.domain.user.dto.response.UserDto;
import com.codeit.otboo.domain.user.entity.User;

@Mapper(componentModel = "spring")
public interface UserMapper {

	/*	@Mapping(target = "id", expression = "java(UUID.randomUUID())")
		@Mapping(target = "passwordHash", source = "password")
		@Mapping(target = "locked", constant = "false")
		@Mapping(target = "role", expression = "java(com.codeit.otboo.global.enumType.Role.USER)")*/
	User toEntity(UserCreateRequest request);

	@Mapping(target = "linkedOAuthProviders", ignore = true)
	UserDto toDto(User user);

	List<UserDto> toDtoList(List<User> users);
}