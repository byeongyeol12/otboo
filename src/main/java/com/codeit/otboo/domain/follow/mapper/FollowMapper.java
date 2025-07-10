package com.codeit.otboo.domain.follow.mapper;

import java.util.List;
import java.util.UUID;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.codeit.otboo.domain.follow.dto.FollowDto;
import com.codeit.otboo.domain.follow.dto.FollowSummaryDto;
import com.codeit.otboo.domain.follow.entity.Follow;
import com.codeit.otboo.domain.user.dto.response.UserSummaryDto;
import com.codeit.otboo.domain.user.entity.User;

@Mapper(componentModel = "spring")
public interface FollowMapper {

	@Mapping(target = "followee", expression = "java(toUserSummary(follow.getFollowee()))")
	@Mapping(target = "follower", expression = "java(toUserSummary(follow.getFollower()))")
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

	default UserSummaryDto toUserSummary(User user) {
		if (user == null)
			return null;
		String profileImgUrl = (user.getProfile() != null) ? user.getProfile().getProfileImageUrl() : null;
		return new UserSummaryDto(user.getId(), user.getName(), profileImgUrl);
	}
}
