package com.codeit.otboo.domain.clothes.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.codeit.otboo.domain.clothes.dto.request.ClothesCreateRequest;
import com.codeit.otboo.domain.clothes.dto.request.ClothesUpdateRequest;
import com.codeit.otboo.domain.clothes.dto.response.ClothesDto;
import com.codeit.otboo.domain.clothes.dto.response.ClothesDtoCursorResponse;
import com.codeit.otboo.domain.clothes.service.ClothesService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/clothes")
public class ClothesController {

	private final ClothesService clothesService;

	@GetMapping
	public ResponseEntity<ClothesDtoCursorResponse> getClothesList(
		@RequestParam("ownerId") UUID ownerId,
		@RequestParam(value = "idAfter", required = false) UUID idAfter,
		@RequestParam(value = "limit", defaultValue = "10") int limit,
		@RequestParam(value = "typeEqual", required = false) String typeEqual) {

		ClothesDtoCursorResponse response = clothesService.getClothesList(ownerId, idAfter, limit, typeEqual);
		return ResponseEntity.ok(response);
	}

	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<ClothesDto> createClothes(
		@RequestPart("request") @Valid ClothesCreateRequest request,
		@RequestPart(value = "image", required = false) MultipartFile image) {

		ClothesDto responseDto = clothesService.createClothes(request, image);
		return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
	}

	@PatchMapping(value = "/{clothesId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<ClothesDto> updateClothes(
		@PathVariable("clothesId") UUID clothesId,
		@RequestParam("ownerId") UUID ownerId,
		@RequestPart("request") @Valid ClothesUpdateRequest request,
		@RequestPart(value = "image", required = false) MultipartFile image) {

		ClothesDto updatedClothes = clothesService.updateClothes(clothesId, ownerId, request, image);
		return ResponseEntity.ok(updatedClothes);
	}

	@DeleteMapping("/{clothesId}")
	public ResponseEntity<Void> deleteClothes(
		@PathVariable("clothesId") UUID clothesId,
		@RequestParam("ownerId") UUID ownerId) {

		clothesService.deleteClothes(clothesId, ownerId);
		return ResponseEntity.noContent().build();
	}

}
