package com.codeit.otboo.domain.follow.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import com.codeit.otboo.domain.follow.dto.FollowDto;
import com.codeit.otboo.domain.follow.entity.Follow;

@Mapper
public interface FollowMapper {
	FollowMapper INSTANCE = Mappers.getMapper(FollowMapper.class);

	FollowDto toFollowDto(Follow follow);
}
