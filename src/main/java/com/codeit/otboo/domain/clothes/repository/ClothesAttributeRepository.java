package com.codeit.otboo.domain.clothes.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.codeit.otboo.domain.clothes.entity.ClothesAttribute;

public interface ClothesAttributeRepository extends JpaRepository<ClothesAttribute, UUID> {
	
}
