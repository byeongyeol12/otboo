package com.codeit.otboo.domain.clothes.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.codeit.otboo.domain.clothes.entity.AttributeDef;

public interface AttributeDefRepository extends JpaRepository<AttributeDef, UUID>, AttributeDefRepositoryCustom {

	Optional<AttributeDef> findByName(String name);
}
