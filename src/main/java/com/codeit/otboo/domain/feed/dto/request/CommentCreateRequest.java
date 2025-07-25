package com.codeit.otboo.domain.feed.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Schema(description = "댓글 작성 요청 DTO")
public class CommentCreateRequest {

	@Schema(description = "댓글을 작성할 피드의 ID", example = "d7cbb9fd-2106-43cd-b38e-1eecbbbe2b92")
	@NotNull(message = "피드 ID는 필수입니다")
	private UUID feedId;

	@Schema(description = "댓글 작성자 ID", example = "a2fa9ee8-d8db-44d7-80b9-63b89b02e88c")
	@NotNull(message = "작성자 ID는 필수입니다")
	private UUID authorId;

	@Schema(description = "댓글 내용", example = "오늘 코디 너무 좋아요!")
	@NotNull(message = "댓글 내용은 필수 입니다.")
	private String content;
}
