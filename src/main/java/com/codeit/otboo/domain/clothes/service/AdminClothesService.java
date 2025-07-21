package com.codeit.otboo.domain.clothes.service;

import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
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
import com.codeit.otboo.domain.notification.dto.NotificationDto;
import com.codeit.otboo.domain.notification.entity.NotificationLevel;
import com.codeit.otboo.domain.notification.service.NotificationService;
import com.codeit.otboo.domain.user.entity.User;
import com.codeit.otboo.domain.user.repository.UserRepository;
import com.codeit.otboo.exception.CustomException;
import com.codeit.otboo.global.error.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class AdminClothesService {

	private final AttributeDefRepository attributeDefRepository;
	private final ClothesRepository clothesRepository;
	private final NotificationService notificationService;
	private final UserRepository userRepository;

	@Transactional
	public ClothesAttributeDefDto createAttributeDef(ClothesAttributeDefCreateRequest request) {
		attributeDefRepository.findByName(request.name()).ifPresent(def -> {
			throw new CustomException(ErrorCode.ATTRIBUTE_DEF_DUPLICATED);
		});

		AttributeDef newDef = new AttributeDef(request.name(), request.selectableValues());
		AttributeDef savedDef = attributeDefRepository.save(newDef);

		User user = userRepository.findByName(request.name());
		// 의상 속성 추가 시 알림 발생
		log.info("의상 생성 알림");
		notificationService.createAndSend(
			new NotificationDto(
				UUID.randomUUID(),
				Instant.now(),
				user.getId(),
				"ClothesAttributeDef",
				"의상 속성 ["+request.name()+"] 이 추가되었습니다.",
				NotificationLevel.INFO
			)
		);

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
		String cursor, int limit, String sortBy, String sortDirection, String keywordLike) {

		UUID idAfter = Optional.ofNullable(cursor)
			.map(c -> UUID.fromString(new String(Base64.getDecoder().decode(c))))
			.orElse(null);

		Sort.Direction direction =
			sortDirection.equalsIgnoreCase("DESCENDING") ? Sort.Direction.DESC : Sort.Direction.ASC;

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

		String nextCursor = Optional.ofNullable(nextIdAfter)
			.map(id -> Base64.getEncoder().encodeToString(id.toString().getBytes()))
			.orElse(null);

		long total = attributeDefRepository.count();

		return new ClothesAttributeDefDtoCursorResponse(
			dtoList, null, nextIdAfter, hasNext, total, sortBy, sortDirection
		);
	}
}
