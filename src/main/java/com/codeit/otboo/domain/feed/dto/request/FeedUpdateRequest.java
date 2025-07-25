package com.codeit.otboo.domain.feed.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Schema(description = "피드 내용 수정 요청 DTO")
public class FeedUpdateRequest {

	@Schema(description = "수정할 피드 내용", example = "날씨가 더워져서 반팔로 바꿨어요!")
	@NotNull(message = "내용을 입력해주세요")
	private String content;
}
