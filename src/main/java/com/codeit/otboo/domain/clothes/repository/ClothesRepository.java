package com.codeit.otboo.domain.clothes.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.codeit.otboo.domain.clothes.entity.Clothes;
import com.codeit.otboo.domain.clothes.entity.ClothesType;

public interface ClothesRepository extends JpaRepository<Clothes, UUID>, ClothesRepositoryCustom {

	List<Clothes> findAllByOwnerId(UUID ownerId);

	Page<Clothes> findAllByOwnerId(UUID ownerId, Pageable pageable);

	Page<Clothes> findAllByOwnerIdAndType(UUID ownerId, ClothesType type, Pageable pageable);

	boolean existsByAttributes_AttributeDef_Id(UUID attributeDefId);

}
