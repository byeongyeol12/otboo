package com.codeit.otboo.domain.clothes.controller;

import com.codeit.otboo.domain.clothes.dto.request.ClothesCreateRequest;
import com.codeit.otboo.domain.clothes.dto.request.ClothesUpdateRequest;
import com.codeit.otboo.domain.clothes.dto.response.ClothesDto;
import com.codeit.otboo.domain.clothes.dto.response.ClothesDtoCursorResponse;
import com.codeit.otboo.domain.clothes.service.ClothesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Tag(name = "의상 관리", description = "사용자의 개별 의상 등록, 조회, 수정, 삭제 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/clothes")
public class ClothesController {

	private final ClothesService clothesService;

	@Operation(summary = "내 옷장 조회", description = "특정 사용자의 옷장 속 의상 목록을 페이지네이션으로 조회합니다.")
	@ApiResponse(responseCode = "200", description = "조회 성공")
	@GetMapping
	public ResponseEntity<ClothesDtoCursorResponse> getClothesList(
			@Parameter(description = "옷장 주인의 사용자 ID") @RequestParam("ownerId") UUID ownerId,
			@Parameter(description = "페이지네이션 커서") @RequestParam(value = "cursor", required = false) String cursor,
			@Parameter(description = "조회할 개수") @RequestParam(value = "limit", defaultValue = "10") int limit,
			@Parameter(description = "의상 종류 필터") @RequestParam(value = "typeEqual", required = false) String typeEqual) {

		ClothesDtoCursorResponse response = clothesService.getClothesList(ownerId, cursor, limit, typeEqual);
		return ResponseEntity.ok(response);
	}

	@Operation(summary = "의상 등록", description = "새로운 의상을 내 옷장에 등록합니다. (이미지 포함)")
	@ApiResponses({
			@ApiResponse(responseCode = "201", description = "의상 등록 성공"),
			@ApiResponse(responseCode = "400", description = "입력값 유효성 검증 실패")
	})
	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<ClothesDto> createClothes(
			@Parameter(description = "의상 정보 JSON") @RequestPart("request") @Valid ClothesCreateRequest request,
			@Parameter(description = "의상 이미지 파일") @RequestPart(value = "image", required = false) MultipartFile image) {

		ClothesDto responseDto = clothesService.createClothes(request, image);
		return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
	}

	@Operation(summary = "의상 정보 수정", description = "등록된 의상의 정보를 수정합니다. (이미지 포함)")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "수정 성공"),
			@ApiResponse(responseCode = "403", description = "자신의 의상만 수정할 수 있음"),
			@ApiResponse(responseCode = "404", description = "해당 의상을 찾을 수 없음")
	})
	@PatchMapping(value = "/{clothesId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<ClothesDto> updateClothes(
			@Parameter(description = "수정할 의상의 ID") @PathVariable("clothesId") UUID clothesId,
			@Parameter(description = "의상 소유자의 ID") @RequestParam("ownerId") UUID ownerId,
			@Parameter(description = "수정할 의상 정보 JSON") @RequestPart("request") @Valid ClothesUpdateRequest request,
			@Parameter(description = "새로 업로드할 의상 이미지 파일") @RequestPart(value = "image", required = false) MultipartFile image) {

		ClothesDto updatedClothes = clothesService.updateClothes(clothesId, ownerId, request, image);
		return ResponseEntity.ok(updatedClothes);
	}

	@Operation(summary = "의상 삭제", description = "등록된 의상을 삭제합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "204", description = "삭제 성공"),
			@ApiResponse(responseCode = "403", description = "자신의 의상만 삭제할 수 있음"),
			@ApiResponse(responseCode = "404", description = "해당 의상을 찾을 수 없음")
	})
	@DeleteMapping("/{clothesId}")
	public ResponseEntity<Void> deleteClothes(
			@Parameter(description = "삭제할 의상의 ID") @PathVariable("clothesId") UUID clothesId,
			@Parameter(description = "의상 소유자의 ID") @RequestParam("ownerId") UUID ownerId) {

		clothesService.deleteClothes(clothesId, ownerId);
		return ResponseEntity.noContent().build();
	}

}