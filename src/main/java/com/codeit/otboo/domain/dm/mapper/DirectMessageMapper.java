package com.codeit.otboo.domain.dm.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.codeit.otboo.domain.dm.dto.DirectMessageDto;
import com.codeit.otboo.domain.dm.entity.Dm;
import com.codeit.otboo.domain.user.dto.response.UserSummaryDto;
import com.codeit.otboo.domain.user.entity.User;

@Mapper(componentModel = "spring")
public interface DirectMessageMapper {
	@Mapping(target = "sender",source = "sender")
	@Mapping(target = "receiver",source = "receiver")
	DirectMessageDto toDirectMessageDto(Dm dm);

	default UserSummaryDto toUserSummary(User user) {
		return new UserSummaryDto(
			user.getId(),
			user.getName(),
			user.getProfile() != null ? user.getProfile().getProfileImageUrl() : null
		);
	}
}
