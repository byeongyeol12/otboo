package com.codeit.otboo.domain.user.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.codeit.otboo.domain.user.dto.request.UserCreateRequest;
import com.codeit.otboo.domain.user.dto.response.UserDto;
import com.codeit.otboo.domain.user.entity.User;

@Mapper(componentModel = "spring")
public interface UserMapper {

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "locked", ignore = true)
	@Mapping(target = "role", ignore = true)
	@Mapping(target = "passwordHash", ignore = true)
	@Mapping(target = "field", ignore = true)
	User toEntity(UserCreateRequest request);

	@Mapping(target = "linkedOAuthProviders", ignore = true)
	UserDto toDto(User user);

	List<UserDto> toDtoList(List<User> users);
}