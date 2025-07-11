package com.codeit.otboo.domain.follow.entity;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.codeit.otboo.domain.user.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
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
@EntityListeners(AuditingEntityListener.class)
public class Follow {
	@Id
	@Column(nullable = false)
	private UUID id;

	@CreatedDate
	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "follower_id", nullable = false)
	private User follower; // 나를 팔로우 한 사람

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "followee_id", nullable = false)
	private User followee; // 내가 팔로우 한 사람


	@Builder
	public Follow(UUID id, User follower, User followee) {
		this.id = id;
		this.follower = follower;
		this.followee = followee;
		this.createdAt = Instant.now();
	}
}
