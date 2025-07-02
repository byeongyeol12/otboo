package com.codeit.otboo.domain.follow.dto;

import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record FollowListResponse(
	@NotNull
	List<FollowDto> data,

	String nextCursor, //다음 커서
	UUID nextIdAfter, //다음 요청의 보조 커서

	@NotNull
	Boolean hasNext, //다음 데이터가 있는지 여부

	@NotNull
	Long totalCount, //총 데이터 개수

	String sortBy, //정렬 기준
	String sortDirection //정렬 방향
) {
}
