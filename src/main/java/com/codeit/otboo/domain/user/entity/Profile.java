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
import lombok.Builder;

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
	@Column(length = 10)
	private Gender gender;

	private Double latitude;
	private Double longitude;
	private Integer x;
	private Integer y;

	@Column(name = "location_names", columnDefinition = "TEXT")
	private String locationNames; // JSON 또는 CSV 저장 → List 변환은 서비스/Mapper에서 처리

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

	protected Profile() {
	}

	public UUID getId() {
		return id;
	}

	public String getNickname() {
		return nickname;
	}

	public Gender getGender() {
		return gender;
	}

	public String getLocationNames() {
		return locationNames;
	}

	public Integer getTemperatureSensitivity() {
		return temperatureSensitivity;
	}

	public User getUser() {
		return user;
	}

	public String getProfileImageUrl() {
		return profileImageUrl;
	}

	// Builder 패턴 생성자
	@Builder
	public Profile(User user, String nickname, Gender gender,
		Double latitude, Double longitude, Integer x, Integer y,
		String locationNames, Integer temperatureSensitivity, String profileImageUrl) {
		this.user = user;
		this.nickname = nickname;
		this.gender = gender;
		this.latitude = latitude;
		this.longitude = longitude;
		this.x = x;
		this.y = y;
		this.locationNames = locationNames;
		this.temperatureSensitivity = temperatureSensitivity;
		this.profileImageUrl = profileImageUrl;
	}
}

