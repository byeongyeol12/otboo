package com.codeit.otboo.domain.clothes.entity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "clothes")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Clothes {

	@Id
	@Column(columnDefinition = "uuid", updatable = false, nullable = false)
	private UUID id;

	@Column(name = "owner_id", columnDefinition = "uuid", nullable = false)
	private UUID ownerId;

	@Column(length = 20, nullable = false)
	private String name;

	@Column(name = "image_url")
	private String imageUrl;

	@Enumerated(EnumType.STRING)
	@Column(name = "type", length = 100, nullable = false)
	private ClothesType type;

	@OneToMany(mappedBy = "clothes", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<ClothesAttribute> attributes = new ArrayList<>();

	@CreatedDate
	@Column(name = "created_at", columnDefinition = "timestamp with time zone", updatable = false, nullable = false)
	private Instant createdAt;

	@LastModifiedDate
	@Column(name = "updated_at", columnDefinition = "timestamp with time zone")
	private Instant updatedAt;

	public Clothes(UUID ownerId, String name, String imageUrl, ClothesType type) {
		this.id = UUID.randomUUID();
		this.ownerId = ownerId;
		this.name = name;
		this.imageUrl = imageUrl;
		this.type = type;
		this.createdAt = Instant.now();
		this.updatedAt = Instant.now();
	}

}
