package com.codeit.otboo.domain.user.entity;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.codeit.otboo.global.enumType.Role;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
@EntityListeners(AuditingEntityListener.class)
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(columnDefinition = "uuid", updatable = false, nullable = false)
	private UUID id;

	@Column(nullable = false, unique = true, length = 255)
	private String email;

	@Column(nullable = false, length = 50)
	private String name;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Role role;

	@Column(nullable = false)
	private boolean locked = false;

	@Column(name = "password_hash", nullable = false, length = 255)
	private String passwordHash;

	@CreatedDate
	@Column(columnDefinition = "timestamp with time zone", name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@Column(length = 255)
	private String field;

	@LastModifiedDate
	@Column(columnDefinition = "timestamp with time zone", name = "updated_at")
	private Instant updatedAt;
	@OneToOne(mappedBy = "user")
	private Profile profile;

	public User() {
	}

	public UUID getId() {
		return id;
	}

	public Profile getProfile() {
		return profile;
	}

	public String getField() {
		return field;
	}

	public String getEmail() {
		return email;
	}

	public String getName() {
		return name;
	}

	public Role getRole() {
		return role;
	}

	public String getPasswordHash() {
		return passwordHash;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public void setLocked(boolean locked) {
		this.locked = locked;
	}

	public void setRole(Role role) {
		this.role = role;
	}

	public void setPasswordHash(String passwordHash) {
		this.passwordHash = passwordHash;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public void setField(String field) {
		this.field = field;
	}

	public boolean isLocked() {
		return locked;
	}

	public void updateRole(Role role) {
		this.role = role;
	}

	public void changePassword(String encodedPassword) {
		this.passwordHash = encodedPassword;
	}
}
