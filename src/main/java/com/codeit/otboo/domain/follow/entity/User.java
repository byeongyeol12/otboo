package com.codeit.otboo.domain.follow.entity;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "사용자")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class User {

	@Id
	@GeneratedValue
	@Column(nullable = false, updatable = false)
	private UUID id;

	@Column(unique = true, length = 255)
	private String email;

	@Column(length = 50, nullable = false)
	private String name;

	/**
	 * 기본값: USER
	 */
	@Column(length = 10, nullable = false)
	private String role = "USER";

	/**
	 * 기본값: false
	 */
	@Column(nullable = false)
	private Boolean locked = false;

	@CreatedDate
	@Column(name = "created_at",
		nullable = false, updatable = false,
		columnDefinition = "TIMESTAMPTZ")
	private Instant createdAt;

	@Column(name = "password_hash", nullable = false, length = 255)
	private String passwordHash;

	@Column(name = "Field", length = 255)
	private String field;

	private String profileImageUrl;
	/**
	 * 빌더에는 비즈니스상 필수인 email, name, passwordHash만 노출
	 */
	@Builder
	public User(String email, String name, String passwordHash) {
		this.email        = email;
		this.name         = name;
		this.passwordHash = passwordHash;
	}
}
