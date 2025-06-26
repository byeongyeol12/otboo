package com.codeit.otboo.domain.user.entity;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.codeit.otboo.global.enumType.Gender;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "profiles")
@EntityListeners(AuditingEntityListener.class)
public class Profile {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(columnDefinition = "uuid", updatable = false, nullable = false)
	private UUID id;

	@Column(nullable = false, length = 50)
	private String nickname;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 10)
	private Gender gender;

	private Double latitude;
	private Double longitude;
	private Integer x;
	private Integer y;

	@Column(name = "location_names")
	private String locationName;

	@Column(name = "profile_img_url")
	private String profileImgUrl;

	@OneToOne
	@JoinColumn(name = "user_id", nullable = false, unique = true)
	private User user;

	@CreatedDate
	@Column(columnDefinition = "timestamp with time zone", name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@LastModifiedDate
	@Column(columnDefinition = "timestamp with time zone", name = "updated_at")
	private Instant updatedAt;
}
