package com.codeit.otboo.domain.clothes.controller;

import com.codeit.otboo.domain.clothes.dto.request.ClothesAttributeDefCreateRequest;
import com.codeit.otboo.domain.clothes.dto.request.ClothesAttributeDefUpdateRequest;
import com.codeit.otboo.domain.clothes.dto.response.ClothesAttributeDefDto;
import com.codeit.otboo.domain.clothes.dto.response.ClothesAttributeDefDtoCursorResponse;
import com.codeit.otboo.domain.clothes.service.AdminClothesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "의상 속성 관리 (관리자용)", description = "관리자가 의상의 속성(종류, 소재 등)을 정의하는 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/clothes/attribute-defs")
public class AdminClothesController {

	private final AdminClothesService adminClothesService;

	@Operation(summary = "의상 속성 정의 생성", description = "새로운 의상 속성(예: '소재')과 선택 가능한 값들(예: ['면', '데님'])을 등록합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "201", description = "속성 정의 생성 성공"),
			@ApiResponse(responseCode = "400", description = "입력값 유효성 검증 실패")
	})
	@PostMapping
	public ResponseEntity<ClothesAttributeDefDto> createAttributeDef(
			@Valid @RequestBody ClothesAttributeDefCreateRequest request) {
		ClothesAttributeDefDto responseDto = adminClothesService.createAttributeDef(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
	}

	@Operation(summary = "의상 속성 정의 목록 조회", description = "등록된 모든 의상 속성 정의를 페이지네이션으로 조회합니다.")
	@ApiResponse(responseCode = "200", description = "조회 성공")
	@GetMapping
	public ResponseEntity<ClothesAttributeDefDtoCursorResponse> getAttributeDefs(
			@Parameter(description = "페이지네이션 커서") @RequestParam(required = false) String cursor,
			@Parameter(description = "조회할 개수") @RequestParam(defaultValue = "10") int limit,
			@Parameter(description = "정렬 기준 필드") @RequestParam(defaultValue = "createdAt") String sortBy,
			@Parameter(description = "정렬 방향") @RequestParam(defaultValue = "DESC") String sortDirection,
			@Parameter(description = "이름 검색 키워드") @RequestParam(required = false) String keywordLike
	) {
		ClothesAttributeDefDtoCursorResponse response = adminClothesService.getAttributeDefsWithCursor(
				cursor, limit, sortBy, sortDirection, keywordLike
		);
		return ResponseEntity.ok(response);
	}

	@Operation(summary = "의상 속성 정의 수정", description = "기존 의상 속성 정의의 이름이나 선택 가능한 값들을 수정합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "수정 성공"),
			@ApiResponse(responseCode = "404", description = "해당 속성 정의를 찾을 수 없음")
	})
	@PatchMapping("/{definitionId}")
	public ResponseEntity<ClothesAttributeDefDto> updateAttributeDef(
			@Parameter(description = "수정할 속성 정의의 ID") @PathVariable UUID definitionId,
			@Valid @RequestBody ClothesAttributeDefUpdateRequest request) {
		ClothesAttributeDefDto responseDto = adminClothesService.updateAttributeDef(definitionId, request);
		return ResponseEntity.ok(responseDto);
	}

	@Operation(summary = "의상 속성 정의 삭제", description = "의상 속성 정의를 삭제합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "204", description = "삭제 성공"),
			@ApiResponse(responseCode = "404", description = "해당 속성 정의를 찾을 수 없음")
	})
	@DeleteMapping("/{definitionId}")
	public ResponseEntity<Void> deleteAttributeDef(@Parameter(description = "삭제할 속성 정의의 ID") @PathVariable UUID definitionId) {
		adminClothesService.deleteAttributeDef(definitionId);
		return ResponseEntity.noContent().build();
	}
}