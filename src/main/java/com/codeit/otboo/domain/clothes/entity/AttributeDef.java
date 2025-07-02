package com.codeit.otboo.domain.clothes.entity;

import java.util.List;
import java.util.UUID;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
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

	@ElementCollection(fetch = FetchType.LAZY)
	@CollectionTable(name = "attribute_def_selectable_values", joinColumns = @JoinColumn(name = "attribute_def_id"))
	@Column
	private List<String> selectableValues;
}
