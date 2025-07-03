package com.codeit.otboo.domain.clothes.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.codeit.otboo.domain.clothes.dto.request.ClothesAttributeDefCreateRequest;
import com.codeit.otboo.domain.clothes.dto.request.ClothesAttributeDefUpdateRequest;
import com.codeit.otboo.domain.clothes.dto.response.ClothesAttributeDefDto;
import com.codeit.otboo.domain.clothes.dto.response.ClothesAttributeDefDtoCursorResponse;
import com.codeit.otboo.domain.clothes.service.AdminClothesService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/clothes/attribute-defs")
public class AdminClothesController {

	private final AdminClothesService adminClothesService;

	@PostMapping
	public ResponseEntity<ClothesAttributeDefDto> createAttributeDef(
		@Valid @RequestBody ClothesAttributeDefCreateRequest request) {
		ClothesAttributeDefDto responseDto = adminClothesService.createAttributeDef(request);

		return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
	}

	@GetMapping
	public ResponseEntity<ClothesAttributeDefDtoCursorResponse> getAttributeDefs(
		@RequestParam(required = false) String cursor,
		@RequestParam(defaultValue = "10") int limit,
		@RequestParam(defaultValue = "createdAt") String sortBy,
		@RequestParam(defaultValue = "DESC") String sortDirection,
		@RequestParam(required = false) String keywordLike
	) {
		ClothesAttributeDefDtoCursorResponse response = adminClothesService.getAttributeDefsWithCursor(
			cursor, limit, sortBy, sortDirection, keywordLike
		);
		return ResponseEntity.ok(response);
	}

	@PatchMapping("/{definitionId}")
	public ResponseEntity<ClothesAttributeDefDto> updateAttributeDef(
		@PathVariable UUID definitionId,
		@Valid @RequestBody ClothesAttributeDefUpdateRequest request) {

		ClothesAttributeDefDto responseDto = adminClothesService.updateAttributeDef(definitionId, request);

		return ResponseEntity.ok(responseDto);
	}

	@DeleteMapping("/{definitionId}")
	public ResponseEntity<Void> deleteAttributeDef(@PathVariable UUID definitionId) {

		adminClothesService.deleteAttributeDef(definitionId);

		return ResponseEntity.noContent().build();
	}

}
