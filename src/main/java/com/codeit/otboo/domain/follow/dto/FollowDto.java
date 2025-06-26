package com.codeit.otboo.domain.follow.dto;

import java.util.UUID;

public record FollowDto(
	UUID id,
	UserSummary followee,
	UserSummary follower
) {

}
