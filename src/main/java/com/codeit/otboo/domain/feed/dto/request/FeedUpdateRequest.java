package com.codeit.otboo.domain.feed.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FeedUpdateRequest {
	@NotNull(message = "내용을 입력해주세요")
	private String content;
}
