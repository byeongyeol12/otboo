package com.codeit.otboo.domain.follow.dto;

import java.util.UUID;

public record FollowSummaryDto(
	UUID followeeId,
	long followerCount,
	long followingCount,
	boolean followerdByMe,
	UUID followedByMeId,
	boolean followingMe
) {
}
