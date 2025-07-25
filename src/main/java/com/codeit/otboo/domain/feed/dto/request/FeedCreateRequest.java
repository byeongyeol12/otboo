package com.codeit.otboo.domain.feed.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Schema(description = "피드 작성 요청 DTO")
public class FeedCreateRequest {

	@Schema(description = "피드 작성자 ID", example = "a2fa9ee8-d8db-44d7-80b9-63b89b02e88c")
	@NotNull(message = "작성자 ID는 필수입니다")
	private UUID authorId;

	@Schema(description = "연관된 날씨 정보 ID", example = "d7cbb9fd-2106-43cd-b38e-1eecbbbe2b92")
	@NotNull(message = "날씨 ID는 필수입니다")
	private UUID weatherId;

	@Schema(description = "피드에 포함된 의류 ID 목록", example = "[\"a0bb2e60-5d98-48ea-a2a8-6c2cb62c67a9\", \"7e1fd180-851e-4ca0-b3b6-42e8ff6bbd8a\"]")
	private List<UUID> clothesIds;

	@Schema(description = "피드 내용", example = "오늘은 쌀쌀해서 긴팔 셔츠를 입었어요!")
	@NotNull(message = "내용을 입력해주세요")
	private String content;
}
