// package com.codeit.otboo.domain.feed.dto.response;
//
// import java.time.Instant;
// import java.util.UUID;
//
// import com.codeit.otboo.domain.feed.entity.FeedComment;
// import com.codeit.otboo.domain.user.dto.response.AuthorDto;
// import com.codeit.otboo.domain.user.entity.Profile;
// import com.codeit.otboo.domain.user.entity.User;
//
// import lombok.AccessLevel;
// import lombok.Getter;
// import lombok.NoArgsConstructor;
//
// @Getter
// @NoArgsConstructor(access = AccessLevel.PROTECTED)
// public class FeedCommentDto {
//
// 	private UUID id;
// 	private Instant createdAt;
// 	private UUID feedId;
// 	private AuthorDto author;
// 	private String content;
//
// 	public FeedCommentDto(UUID id, Instant createdAt, UUID feedId, AuthorDto author, String content) {
// 		this.id = id;
// 		this.createdAt = createdAt;
// 		this.feedId = feedId;
// 		this.author = author;
// 		this.content = content;
// 	}
//
// 	public static CommentDto fromEntity(FeedComment comment) {
// 		User user = comment.getAuthor();
// 		Profile profile = user.getProfile();
// 		AuthorDto author = new AuthorDto(user.getId(), user.getName(), user.getProfile().getProfileImageUrl());
//
// 		return new CommentDto(
// 			comment.getId(),
// 			comment.getCreatedAt(),
// 			comment.getFeed().getId(),
// 			author,
// 			comment.getContent()
// 		);
// 	}
// }