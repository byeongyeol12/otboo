package com.codeit.otboo.domain.notification.entity;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.codeit.otboo.domain.user.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "notification")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Notification {

	@Id
	@GeneratedValue
	@Column(nullable = false)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(length = 100, nullable = false)
	private String title;

	@Column(columnDefinition = "TEXT", nullable = false)
	private String content;

	@Column(length = 10, nullable = false)
	private String level;

	@Column(nullable = false)
	private Boolean confirmed = false;

	@CreatedDate
	@Column(name = "created_at",
		nullable = false, updatable = false,
		columnDefinition = "TIMESTAMPTZ")
	private Instant createdAt;

	@LastModifiedDate
	@Column(name = "updated_at",
		columnDefinition = "TIMESTAMPTZ")
	private Instant updatedAt;

	@Builder
	public Notification(User user, String title, String content, String level) {
		this.user = user;
		this.title = title;
		this.content = content;
		this.level = level;
		this.confirmed = false;
	}
}
