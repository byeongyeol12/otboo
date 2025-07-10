package com.codeit.otboo.domain.feed.dto.request;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommentCreateRequest {

	@NotNull(message = "피드 ID는 필수입니다")
	private UUID feedId;

	@NotNull(message = "작성자 ID는 필수입니다")
	private UUID authorId;

	@NotNull(message = "댓글 내용은 필수 입니다.")
	private String content;

}
