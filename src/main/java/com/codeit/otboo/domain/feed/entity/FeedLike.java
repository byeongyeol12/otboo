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
@Table(name = "feed_likes")
public class FeedLike extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "feed_id", nullable = false)
	private Feed feed;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	public FeedLike(Feed feed, User user) {
		this.feed = feed;
		this.user = user;
	}
}
