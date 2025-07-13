package com.codeit.otboo.domain.feed.entity;

import com.codeit.otboo.domain.user.entity.User;
import com.codeit.otboo.global.base.BaseEntity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "feed_comments")
public class FeedComment extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "feed_id", nullable = false)
	private Feed feed;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "author_id", nullable = false)
	private User author;

	@Column(name = "content", nullable = false)
	private String content;

	public FeedComment(Feed feed, User author, String content) {
		this.feed = feed;
		this.author = author;
		this.content = content;
	}
}
