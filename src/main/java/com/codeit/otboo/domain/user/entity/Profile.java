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
import lombok.Getter;

@Entity
@Table(name = "profiles")
@EntityListeners(AuditingEntityListener.class)
@Getter
public class Profile {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(columnDefinition = "uuid", updatable = false, nullable = false)
	private UUID id;

	@Column(nullable = false, length = 50)
	private String nickname;

	@Enumerated(EnumType.STRING)
	@Column(length = 10)
	private Gender gender;

	@Column(name = "birth_date")
	private Instant birthDate;

	private Double latitude;
	private Double longitude;
	private Integer x;
	private Integer y;

	@Column(name = "location_names", columnDefinition = "TEXT")
	private String locationNames;

	@Column(name = "temp_sensitivity", nullable = false)
	private Integer temperatureSensitivity = 3;

	@Column(name = "profile_img_url", columnDefinition = "TEXT")
	private String profileImageUrl;

	@OneToOne
	@JoinColumn(name = "user_id", nullable = false, unique = true)
	private User user;

	@CreatedDate
	@Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMPTZ")
	private Instant createdAt;

	@LastModifiedDate
	@Column(name = "updated_at", columnDefinition = "TIMESTAMPTZ")
	private Instant updatedAt;

	public Profile() {
	}

	public Profile(User user, String nickname, Gender gender) {
		this.user = user;
		this.nickname = nickname;
		this.gender = gender;
		this.temperatureSensitivity = 3;
		this.latitude = null;
		this.longitude = null;
		this.x = null;
		this.y = null;
		this.locationNames = "";
		this.profileImageUrl = null;
	}

	public void updateProfile(String nickname, Gender gender, Instant birthDate,
		String locationNames, Integer temperatureSensitivity, String profileImageUrl) {
		this.nickname = nickname;
		this.gender = gender;
		this.birthDate = birthDate;
		this.locationNames = locationNames;
		this.temperatureSensitivity = temperatureSensitivity;
		this.profileImageUrl = profileImageUrl;
	}

	public void profileImageUrlUpdate(String imageUrl) {
		this.profileImageUrl = imageUrl;
	}

	public void updateLocation(Double latitude, Double longitude, Integer x, Integer y, String locationNames) {
		this.latitude = latitude;
		this.longitude = longitude;
		this.x = x;
		this.y = y;
		this.locationNames = locationNames;
	}
}

