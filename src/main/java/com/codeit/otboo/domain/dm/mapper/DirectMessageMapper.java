package com.codeit.otboo.domain.dm.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.codeit.otboo.domain.dm.dto.DirectMessageDto;
import com.codeit.otboo.domain.dm.entity.Dm;
import com.codeit.otboo.domain.follow.dto.UserSummary;
import com.codeit.otboo.domain.user.entity.User;

@Mapper(componentModel = "spring")
public interface DirectMessageMapper {
	@Mapping(target = "sender",source = "sender")
	@Mapping(target = "receiver",source = "receiver")
	DirectMessageDto toDirectMessageDto(Dm dm);

	default UserSummary toUserSummary(User user) {
		return new UserSummary(
			user.getId(),
			user.getName(),
			user.getProfile() != null ? user.getProfile().getProfileImgUrl() : null
		);
	}
}
