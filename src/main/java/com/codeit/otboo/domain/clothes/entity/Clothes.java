package com.codeit.otboo.domain.clothes.entity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "clothes")
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Clothes {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(name = "owner_id", nullable = false)
	private UUID ownerId;

	@Column(length = 50, nullable = false)
	private String name;

	@Column(name = "image_url")
	private String imageUrl;

	@Enumerated(EnumType.STRING)
	@Column(name = "type", length = 100, nullable = false)
	private ClothesType type;

	@OneToMany(mappedBy = "clothes", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<ClothesAttribute> attributes = new ArrayList<>();

	@CreatedDate
	@Column(name = "created_at", updatable = false, nullable = false)
	private Instant createdAt;

	@LastModifiedDate
	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	public void addAttribute(AttributeDef attributeDef, String value) {
		ClothesAttribute newAttribute = new ClothesAttribute(this, attributeDef, value);
		this.attributes.add(newAttribute);
	}

	public void clearAttributes() {
		this.attributes.clear();
	}

	public void update(String name, ClothesType type, String imageUrl) {
		this.name = name;
		this.type = type;
		this.imageUrl = imageUrl;
	}

	@Builder
	public Clothes(UUID ownerId, String name, String imageUrl, ClothesType type) {
		this.ownerId = ownerId;
		this.name = name;
		this.imageUrl = imageUrl;
		this.type = type;
	}
}
