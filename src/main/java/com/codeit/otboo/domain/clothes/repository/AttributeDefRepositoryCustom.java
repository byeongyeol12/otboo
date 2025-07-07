package com.codeit.otboo.domain.clothes.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Sort;

import com.codeit.otboo.domain.clothes.entity.AttributeDef;

public interface AttributeDefRepositoryCustom {
	List<AttributeDef> findPagedAttributeDefs(UUID idAfter, int limit, String sortBy, Sort.Direction direction,
		String keywordLike);
}
