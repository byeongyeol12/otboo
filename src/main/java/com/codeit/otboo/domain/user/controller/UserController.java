package com.codeit.otboo.domain.user.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

	private final UserService userService;

	@PostMapping
	public ResponseEntity<UserDto> singup(@Valid @RequestBody UserCreateRequest request) {
		UserDto userDto = userService.create(request);

		return ResponseEntity.status(HttpStatus.CREATED).body(userDto);
	}

	@GetMapping
	//@PreAuthorize("hasRole(ADMIN)")
	public ResponseEntity<UserDtoCursorResponse> getAllUsers(
		@Valid @ModelAttribute UserSearchRequest request
	) {
		UserDtoCursorResponse response = userService.searchUsers(request);
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	@PatchMapping("/{userId}/role")
	//@PreAuthorize("hasRole(ADMIN)")
	public ResponseEntity<UserDto> updateUserRole(
		@PathVariable UUID userId,
		@RequestBody @Valid UserRoleUpdateRequest request
	) {
		UserDto updateUser = userService.updateUserRole(userId, request);
		return ResponseEntity.ok(updateUser);
	}

	@PatchMapping("/{userId}/lock")
	@PreAuthorize("hasRole(ADMIN)")
	public ResponseEntity<UserIdResponse> updateUserLock(
		@PathVariable UUID userId,
		@RequestBody UserLockRequest request,
		@RequestHeader("Authorization") String authorizationHeader
	) {
		String accessToken = authorizationHeader.substring(7);
		User user = userService.updateUserLock(userId, request.locked(), accessToken);
		return ResponseEntity.ok(UserIdResponse.from(user));
	}

	@GetMapping("/{userId}/profiles")
	public ResponseEntity<ProfileDto> getProfile(@PathVariable UUID userId) {
		ProfileDto profileDto = userService.getProfile(userId);
		return ResponseEntity.ok(profileDto);
	}

	@PatchMapping("/{userId}/profiles")
	public ResponseEntity<ProfileDto> updateProfile(
		@PathVariable UUID userId,
		@RequestBody @Valid ProfileUpdateRequest request
	) {
		ProfileDto profileDto = userService.updateProfile(userId, request);
		return ResponseEntity.ok(profileDto);
	}

	@PatchMapping("/{userId}/password")
	public ResponseEntity<Void> changePassword(
		@PathVariable UUID userId,
		@Valid @RequestBody PasswordRequest request,
		@AuthenticationPrincipal UserPrincipal principal
	) {
		userService.changePassword(userId, request, principal);
		return ResponseEntity.noContent().build();
	}
}
