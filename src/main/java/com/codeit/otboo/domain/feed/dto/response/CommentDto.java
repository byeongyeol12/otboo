package com.codeit.otboo.domain.feed.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;
import com.codeit.otboo.domain.feed.entity.FeedComment;
import com.codeit.otboo.domain.user.dto.response.AuthorDto;
import com.codeit.otboo.domain.user.entity.User;

@Schema(description = "댓글 응답 DTO")
public record CommentDto(

		@Schema(description = "댓글 ID", example = "dbe80e27-5e4e-41d8-afe2-8b8c370f3c3f")
		UUID id,

		@Schema(description = "댓글 생성 시각(UTC ISO8601)", example = "2024-07-25T11:40:31.000Z")
		Instant createdAt,

		@Schema(description = "댓글이 달린 피드의 ID", example = "a1b3c5d7-e9f0-1234-abcd-9f9e8b7a6c5d")
		UUID feedId,

		@Schema(description = "댓글 작성자 정보")
		AuthorDto author,

		@Schema(description = "댓글 내용", example = "좋은 코디네요!")
		String content

) {
	public static CommentDto fromEntity(FeedComment comment) {
		User user = comment.getAuthor();
		AuthorDto author = new AuthorDto(
				user.getId(),
				user.getName(),
				user.getProfile().getProfileImageUrl()
		);

		return new CommentDto(
				comment.getId(),
				comment.getCreatedAt(),
				comment.getFeed().getId(),
				author,
				comment.getContent()
		);
	}
}
