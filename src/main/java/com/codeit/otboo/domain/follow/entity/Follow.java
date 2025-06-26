package com.codeit.otboo.domain.follow.entity;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
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

	@Column(nullable = false)
	private UUID followerId; // 나를 팔로우 한 사람

	@Column(nullable = false)
	private UUID followingId; // 내가 팔로우 한 사람

	// @ManyToOne(fetch = FetchType.LAZY)
	// @JoinColumn(name = "followerId", nullable = false)
	// private User follower;
	//
	// @ManyToOne(fetch = FetchType.LAZY)
	// @JoinColumn(name = "followingId", nullable = false)
	// private User following;

	@Builder
	public Follow(UUID followerId, UUID followingId) {
		this.id = UUID.randomUUID();
		this.followerId = followerId;
		this.followingId = followingId;
	}
}
