package com.codeit.otboo.domain.follow.dto;

import java.util.UUID;

public record FollowDto(
	UUID id,
	UUID followee,
	UUID follower
) {

}
