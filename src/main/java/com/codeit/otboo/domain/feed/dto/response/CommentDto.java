package com.codeit.otboo.domain.feed.dto.response;

import java.time.Instant;
import java.util.UUID;

import com.codeit.otboo.domain.feed.entity.FeedComment;
import com.codeit.otboo.domain.user.dto.response.AuthorDto;
import com.codeit.otboo.domain.user.entity.User;

public record CommentDto(
	UUID id,
	Instant createdAt,
	UUID feedId,
	AuthorDto author,
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
