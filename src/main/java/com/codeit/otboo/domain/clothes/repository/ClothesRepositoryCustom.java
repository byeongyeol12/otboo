package com.codeit.otboo.domain.clothes.repository;

import java.util.List;
import java.util.UUID;

import com.codeit.otboo.domain.clothes.entity.Clothes;
import com.codeit.otboo.domain.clothes.entity.ClothesType;

public interface ClothesRepositoryCustom {
	List<Clothes> findClothesByOwnerWithCursor(UUID ownerId, UUID idAfter, int limit, ClothesType type);
}
