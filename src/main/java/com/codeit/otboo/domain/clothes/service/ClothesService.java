package com.codeit.otboo.domain.clothes.service;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.codeit.otboo.domain.clothes.dto.request.ClothesCreateRequest;
import com.codeit.otboo.domain.clothes.dto.request.ClothesUpdateRequest;
import com.codeit.otboo.domain.clothes.dto.response.ClothesAttributeWithDefDto;
import com.codeit.otboo.domain.clothes.dto.response.ClothesDto;
import com.codeit.otboo.domain.clothes.dto.response.ClothesDtoCursorResponse;
import com.codeit.otboo.domain.clothes.entity.AttributeDef;
import com.codeit.otboo.domain.clothes.entity.Clothes;
import com.codeit.otboo.domain.clothes.entity.ClothesType;
import com.codeit.otboo.domain.clothes.repository.AttributeDefRepository;
import com.codeit.otboo.domain.clothes.repository.ClothesRepository;
import com.codeit.otboo.exception.CustomException;
import com.codeit.otboo.global.error.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClothesService {

	private final ClothesRepository clothesRepository;
	private final AttributeDefRepository attributeDefRepository;
	private final ImageUploadService imageUploadService;

	@Transactional
	public ClothesDto createClothes(ClothesCreateRequest request, MultipartFile image) {
		UUID ownerId = request.ownerId();
		String imageUrl = imageUploadService.upload(image);

		Clothes newClothes = Clothes.builder()
			.ownerId(ownerId)
			.name(request.name())
			.type(ClothesType.valueOf(request.type().trim().toUpperCase()))
			.imageUrl(imageUrl)
			.build();

		if (request.attributes() != null) {
			request.attributes().forEach(clothesAttributeDto -> {
				AttributeDef attributeDef = attributeDefRepository.findById(clothesAttributeDto.definitionId())
					.orElseThrow(() -> new CustomException(ErrorCode.ATTRIBUTE_DEF_NOT_FOUND));
				newClothes.addAttribute(attributeDef, clothesAttributeDto.value());
			});
		}

		Clothes savedClothes = clothesRepository.save(newClothes);
		return convertToDto(savedClothes);
	}

	public ClothesDtoCursorResponse getClothesList(UUID ownerId, UUID idAfter, int limit, String typeEqual) {
		ClothesType type = StringUtils.hasText(typeEqual) ? ClothesType.valueOf(typeEqual.toUpperCase()) : null;

		List<Clothes> clothesList = clothesRepository.findClothesByOwnerWithCursor(ownerId, idAfter, limit + 1, type);

		boolean hasNext = clothesList.size() > limit;
		if (hasNext) {
			clothesList.remove(limit);
		}

		List<ClothesDto> data = clothesList.stream().map(this::convertToDto).toList();

		UUID nextIdAfter = null;
		if (hasNext && !data.isEmpty()) {
			nextIdAfter = data.get(data.size() - 1).id();
		}

		return new ClothesDtoCursorResponse(
			data,
			null,
			nextIdAfter,
			hasNext,
			0L,
			"id", // 정렬 기준
			"ASCENDING" // 정렬 방향
		);
	}

	@Transactional
	public ClothesDto updateClothes(UUID clothesId, UUID ownerId, ClothesUpdateRequest request, MultipartFile image) {
		Clothes clothesToUpdate = clothesRepository.findById(clothesId)
			.orElseThrow(() -> new CustomException(ErrorCode.CLOTHES_NOT_FOUND));

		if (!clothesToUpdate.getOwnerId().equals(ownerId)) {
			throw new CustomException(ErrorCode.CLOTHES_FORBIDDEN);
		}

		String imageUrl = clothesToUpdate.getImageUrl();
		if (image != null && !image.isEmpty()) {
			imageUrl = imageUploadService.upload(image);
		}

		clothesToUpdate.update(request.name(), ClothesType.valueOf(request.type().toUpperCase()), imageUrl);

		clothesToUpdate.clearAttributes();
		if (request.attributes() != null) {
			request.attributes().forEach(attrDto -> {
				AttributeDef attributeDef = attributeDefRepository.findById(attrDto.definitionId())
					.orElseThrow(
						() -> new CustomException(ErrorCode.ATTRIBUTE_DEF_NOT_FOUND));
				clothesToUpdate.addAttribute(attributeDef, attrDto.value());
			});
		}
		return convertToDto(clothesToUpdate);
	}

	@Transactional
	public void deleteClothes(UUID clothesId, UUID ownerId) {
		Clothes clothesToDelete = clothesRepository.findById(clothesId)
			.orElseThrow(() -> new CustomException(ErrorCode.CLOTHES_NOT_FOUND));

		if (!clothesToDelete.getOwnerId().equals(ownerId)) {
			throw new CustomException(ErrorCode.CLOTHES_FORBIDDEN);
		}

		clothesRepository.delete(clothesToDelete);
	}

	private ClothesDto convertToDto(Clothes clothes) {
		if (clothes == null)
			return null;

		List<ClothesAttributeWithDefDto> attributeDtos = clothes.getAttributes().stream()
			.map(attribute -> {
				List<String> selectableValues = attribute.getAttributeDef().getSelectableValues();

				return new ClothesAttributeWithDefDto(
					attribute.getAttributeDef().getId(),
					attribute.getAttributeDef().getName(),
					selectableValues != null ? selectableValues : Collections.emptyList(),
					attribute.getValue()
				);
			})
			.toList();

		return new ClothesDto(
			clothes.getId(),
			clothes.getOwnerId(),
			clothes.getName(),
			clothes.getImageUrl(),
			clothes.getType().name(),
			attributeDtos
		);
	}
}
