package com.codeit.otboo.domain.clothes.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.codeit.otboo.domain.clothes.entity.ClothesAttribute;

public interface ClothesAttributeRepository extends JpaRepository<ClothesAttribute, UUID> {

	@Query("SELECT ca FROM ClothesAttribute ca JOIN FETCH ca.attributeDef WHERE ca.clothes.id IN :clothesIds")
	List<ClothesAttribute> findByClothesIdIn(@Param("clothesIds") List<UUID> clothesIds);
}
