package com.codeit.otboo.domain.clothes.entity;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.Type;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import io.hypersistence.utils.hibernate.type.array.ListArrayType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "clothes_attribute_definitions")
public class AttributeDef {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(name = "name", unique = true, nullable = false, length = 50)
	private String name;

	@Type(ListArrayType.class)
	@Column(name = "selectable_values", columnDefinition = "text[]", nullable = false)
	private List<String> selectableValues;

	@CreatedDate
	@Column(name = "created_at", updatable = false, nullable = false)
	private Instant createdAt;

	@LastModifiedDate
	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	public AttributeDef(String name, List<String> selectableValues) {
		this.name = name;
		this.selectableValues = selectableValues;
	}

	public void update(String name, List<String> selectableValues) {
		this.name = name;
		this.selectableValues = selectableValues;
	}

}
