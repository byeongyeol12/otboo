package com.codeit.otboo.domain.follow.mapper;

import java.util.List;
import java.util.UUID;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import com.codeit.otboo.domain.follow.dto.FollowDto;
import com.codeit.otboo.domain.follow.dto.FollowSummaryDto;
import com.codeit.otboo.domain.follow.dto.UserSummaryTemp;
import com.codeit.otboo.domain.follow.entity.Follow;

@Mapper(componentModel = "spring")
public interface FollowMapper {
	FollowMapper INSTANCE = Mappers.getMapper(FollowMapper.class);

	@Mapping(target = "followee", expression = "java(toUserSummaryTemp(follow.getFollowee()))")
	@Mapping(target = "follower", expression = "java(toUserSummaryTemp(follow.getFollower()))")
	FollowDto toFollowDto(Follow follow);

	List<FollowDto> toFollowDtoList(List<Follow> follows);

	FollowSummaryDto toFollowSummaryDto(
		UUID followeeId,
		Long followerCount,
		Long followingCount,
		Boolean followedByMe,
		UUID followedByMeId,
		Boolean followingMe
	);

	default UserSummaryTemp toUserSummaryTemp(User user) {
		if (user == null) return null;
		String imgUrl = null;
		if (user.getProfile() != null) {
			imgUrl = user.getProfile().getProfileImgUrl();
		}
		return new UserSummaryTemp(
			user.getId(),
			user.getName(),
			imgUrl
		);
	}
}
