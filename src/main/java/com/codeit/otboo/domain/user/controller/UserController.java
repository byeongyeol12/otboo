package com.codeit.otboo.domain.user.controller;

import com.codeit.otboo.domain.user.dto.request.PasswordRequest;
import com.codeit.otboo.domain.user.dto.request.ProfileUpdateRequest;
import com.codeit.otboo.domain.user.dto.request.UserCreateRequest;
import com.codeit.otboo.domain.user.dto.request.UserLockRequest;
import com.codeit.otboo.domain.user.dto.request.UserRoleUpdateRequest;
import com.codeit.otboo.domain.user.dto.request.UserSearchRequest;
import com.codeit.otboo.domain.user.dto.response.ProfileDto;
import com.codeit.otboo.domain.user.dto.response.UserDto;
import com.codeit.otboo.domain.user.dto.response.UserDtoCursorResponse;
import com.codeit.otboo.domain.user.dto.response.UserIdResponse;
import com.codeit.otboo.domain.user.entity.User;
import com.codeit.otboo.domain.user.service.UserService;
import com.codeit.otboo.global.config.security.UserPrincipal;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Tag(name = "사용자", description = "사용자 계정 및 프로필 관련 API")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

	private final UserService userService;

	@Operation(summary = "회원가입", description = "새로운 사용자를 등록합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "201", description = "회원가입 성공"),
			@ApiResponse(responseCode = "400", description = "입력값 유효성 검증 실패 또는 이메일 중복")
	})
	@PostMapping
	public ResponseEntity<UserDto> singup(@Valid @RequestBody UserCreateRequest request) {
		UserDto userDto = userService.create(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(userDto);
	}

	@Operation(summary = "사용자 목록 조회 (관리자용)", description = "다양한 조건으로 사용자 목록을 검색하고 조회합니다.")
	@ApiResponse(responseCode = "200", description = "조회 성공")
	@GetMapping
	//@PreAuthorize("hasRole(ADMIN)")
	public ResponseEntity<UserDtoCursorResponse> getAllUsers(
			@Parameter(description = "검색 및 페이징 조건") @Valid @ModelAttribute UserSearchRequest request
	) {
		UserDtoCursorResponse response = userService.searchUsers(request);
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	@Operation(summary = "사용자 권한 변경 (관리자용)", description = "특정 사용자의 권한을 변경합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "권한 변경 성공"),
			@ApiResponse(responseCode = "404", description = "해당 사용자를 찾을 수 없음")
	})
	@PatchMapping("/{userId}/role")
	//@PreAuthorize("hasRole(ADMIN)")
	public ResponseEntity<UserDto> updateUserRole(
			@Parameter(description = "사용자 ID") @PathVariable UUID userId,
			@RequestBody @Valid UserRoleUpdateRequest request
	) {
		UserDto updateUser = userService.updateUserRole(userId, request);
		return ResponseEntity.ok(updateUser);
	}

	@Operation(summary = "사용자 계정 잠금/해제 (관리자용)", description = "특정 사용자의 계정을 잠그거나 해제합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "상태 변경 성공"),
			@ApiResponse(responseCode = "404", description = "해당 사용자를 찾을 수 없음")
	})
	@PatchMapping("/{userId}/lock")
	@PreAuthorize("hasRole(ADMIN)")
	public ResponseEntity<UserIdResponse> updateUserLock(
			@Parameter(description = "사용자 ID") @PathVariable UUID userId,
			@RequestBody UserLockRequest request,
			@Parameter(hidden = true) @RequestHeader("Authorization") String authorizationHeader
	) {
		String accessToken = authorizationHeader.substring(7);
		User user = userService.updateUserLock(userId, request.locked(), accessToken);
		return ResponseEntity.ok(UserIdResponse.from(user));
	}

	@Operation(summary = "프로필 조회", description = "특정 사용자의 프로필 정보를 조회합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "조회 성공"),
			@ApiResponse(responseCode = "404", description = "해당 사용자를 찾을 수 없음")
	})
	@GetMapping("/{userId}/profiles")
	public ResponseEntity<ProfileDto> getProfile(@Parameter(description = "사용자 ID") @PathVariable UUID userId) {
		ProfileDto profileDto = userService.getProfile(userId);
		return ResponseEntity.ok(profileDto);
	}

	@Operation(summary = "프로필 수정", description = "자신의 프로필 정보를 수정합니다. (프로필 사진 포함)")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "수정 성공"),
			@ApiResponse(responseCode = "404", description = "해당 사용자를 찾을 수 없음")
	})
	@PatchMapping(value = "/{userId}/profiles", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<ProfileDto> updateProfile(
			@Parameter(description = "사용자 ID") @PathVariable UUID userId,
			@RequestPart("request") @Valid ProfileUpdateRequest request,
			@Parameter(description = "업로드할 프로필 이미지 파일") @RequestPart(value = "image", required = false) MultipartFile profileImage
	) {
		ProfileDto updated = userService.updateProfile(userId, request, profileImage);
		return ResponseEntity.ok(updated);
	}

	@Operation(summary = "비밀번호 변경", description = "로그인한 사용자의 비밀번호를 변경합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "204", description = "비밀번호 변경 성공"),
			@ApiResponse(responseCode = "403", description = "자신의 비밀번호만 변경할 수 있음")
	})
	@PatchMapping("/{userId}/password")
	public ResponseEntity<Void> changePassword(
			@Parameter(description = "사용자 ID") @PathVariable UUID userId,
			@Valid @RequestBody PasswordRequest request,
			@Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal principal
	) {
		userService.changePassword(userId, request, principal);
		return ResponseEntity.noContent().build();
	}
}