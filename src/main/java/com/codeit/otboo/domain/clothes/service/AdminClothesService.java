package com.codeit.otboo.domain.clothes.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.codeit.otboo.domain.clothes.dto.request.ClothesAttributeDefCreateRequest;
import com.codeit.otboo.domain.clothes.dto.request.ClothesAttributeDefUpdateRequest;
import com.codeit.otboo.domain.clothes.dto.response.ClothesAttributeDefDto;
import com.codeit.otboo.domain.clothes.dto.response.ClothesAttributeDefDtoCursorResponse;
import com.codeit.otboo.domain.clothes.entity.AttributeDef;
import com.codeit.otboo.domain.clothes.repository.AttributeDefRepository;
import com.codeit.otboo.domain.clothes.repository.ClothesRepository;
import com.codeit.otboo.exception.CustomException;
import com.codeit.otboo.global.error.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminClothesService {

	private final AttributeDefRepository attributeDefRepository;
	private final ClothesRepository clothesRepository;

	@Transactional
	public ClothesAttributeDefDto createAttributeDef(ClothesAttributeDefCreateRequest request) {
		attributeDefRepository.findByName(request.name()).ifPresent(def -> {
			throw new CustomException(ErrorCode.ATTRIBUTE_DEF_DUPLICATED);
		});

		AttributeDef newDef = new AttributeDef(request.name(), request.selectableValues());
		AttributeDef savedDef = attributeDefRepository.save(newDef);

		return new ClothesAttributeDefDto(
			savedDef.getId(),
			savedDef.getName(),
			savedDef.getSelectableValues()
		);
	}

	@Transactional(readOnly = true)
	public List<ClothesAttributeDefDto> getAllAttributeDefs() {
		List<AttributeDef> allDefs = attributeDefRepository.findAll();

		return allDefs.stream()
			.map(def -> new ClothesAttributeDefDto(
				def.getId(),
				def.getName(),
				def.getSelectableValues()
			))
			.collect(Collectors.toList());
	}

	@Transactional
	public ClothesAttributeDefDto updateAttributeDef(UUID definitionId, ClothesAttributeDefUpdateRequest request) {
		AttributeDef defToUpdate = attributeDefRepository.findById(definitionId)
			.orElseThrow(() -> new CustomException(ErrorCode.ATTRIBUTE_DEF_NOT_FOUND));

		attributeDefRepository.findByName(request.name()).ifPresent(duplicate -> {
			if (!duplicate.getId().equals(definitionId)) {
				throw new CustomException(ErrorCode.ATTRIBUTE_DEF_DUPLICATED);
			}
		});

		defToUpdate.update(request.name(), request.selectableValues());

		return new ClothesAttributeDefDto(
			defToUpdate.getId(),
			defToUpdate.getName(),
			defToUpdate.getSelectableValues()
		);
	}

	@Transactional
	public void deleteAttributeDef(UUID definitionId) {
		if (clothesRepository.existsByAttributes_AttributeDef_Id(definitionId)) {
			throw new CustomException(ErrorCode.ATTRIBUTE_DEF_IN_USE);
		}

		if (!attributeDefRepository.existsById(definitionId)) {
			throw new CustomException(ErrorCode.ATTRIBUTE_DEF_NOT_FOUND);
		}
		attributeDefRepository.deleteById(definitionId);
	}

	@Transactional(readOnly = true)
	public ClothesAttributeDefDtoCursorResponse getAttributeDefsWithCursor(
		UUID idAfter, int limit, String sortBy, String sortDirection, String keywordLike) {

		Sort.Direction direction =
			sortDirection.equalsIgnoreCase("DESCENDING") ? Sort.Direction.DESC : Sort.Direction.ASC;

		List<String> allowedSortFields = List.of("name", "createdAt");
		if (!allowedSortFields.contains(sortBy)) {
			throw new CustomException(ErrorCode.ATTRIBUTE_DEF_INVALID_SORT_FIELD);
		}

		List<AttributeDef> result = attributeDefRepository.findPagedAttributeDefs(
			idAfter, limit + 1, sortBy, direction, keywordLike
		);

		boolean hasNext = result.size() > limit;
		if (hasNext)
			result.remove(result.size() - 1);

		UUID nextIdAfter = hasNext && !result.isEmpty() ? result.get(result.size() - 1).getId() : null;

		List<ClothesAttributeDefDto> dtoList = result.stream()
			.map(def -> new ClothesAttributeDefDto(def.getId(), def.getName(), def.getSelectableValues()))
			.toList();

		long total = attributeDefRepository.count();

		return new ClothesAttributeDefDtoCursorResponse(
			dtoList, null, nextIdAfter, hasNext, total, sortBy, sortDirection
		);
	}
}
