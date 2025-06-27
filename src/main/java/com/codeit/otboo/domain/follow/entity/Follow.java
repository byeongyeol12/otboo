package com.codeit.otboo.domain.follow.entity;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "follows")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Follow {
	@Id
	@Column(nullable = false)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "followerId", nullable = false)
	private User follower; // 나를 팔로우 한 사람

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "followingId", nullable = false)
	private User followee; // 내가 팔로우 한 사람

	@Builder
	public Follow(User follower, User followee) {
		this.id = UUID.randomUUID();
		this.follower = follower;
		this.followee = followee;
	}
}
