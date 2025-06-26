package com.codeit.otboo.domain.clothes.entity;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "clothes_attribute_values")
public class ClothesAttribute {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "clothes_id", nullable = false)
	private Clothes clothes;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "attribute_definition_id", nullable = false)
	private AttributeDef attributeDef;

	@Column(nullable = false)
	private String value;

	@CreatedDate
	@Column(name = "created_at", updatable = false, nullable = false)
	private Instant createdAt;

	@LastModifiedDate
	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	public ClothesAttribute(Clothes clothes, AttributeDef attributeDef, String value) {
		this.clothes = clothes;
		this.attributeDef = attributeDef;
		this.value = value;
	}
}
