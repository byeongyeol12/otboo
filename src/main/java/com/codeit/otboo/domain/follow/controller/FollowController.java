package com.codeit.otboo.domain.follow.controller;

import com.codeit.otboo.domain.follow.dto.FollowCreateRequest;
import com.codeit.otboo.domain.follow.dto.FollowDto;
import com.codeit.otboo.domain.follow.dto.FollowListResponse;
import com.codeit.otboo.domain.follow.dto.FollowSummaryDto;
import com.codeit.otboo.domain.follow.service.FollowService;
import com.codeit.otboo.global.config.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "팔로우", description = "사용자 팔로우/팔로워 관련 API")
@RestController
@RequestMapping("/api/follows")
@RequiredArgsConstructor
public class FollowController {
	private final FollowService followService;

	@Operation(summary = "사용자 팔로우", description = "다른 사용자를 팔로우합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "201", description = "팔로우 성공"),
			@ApiResponse(responseCode = "400", description = "자기 자신을 팔로우할 수 없음"),
			@ApiResponse(responseCode = "404", description = "팔로우할 사용자를 찾을 수 없음")
	})
	@PostMapping
	public ResponseEntity<FollowDto> createFollow(
			@Valid @RequestBody FollowCreateRequest request,
			@Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal userPrincipal
	) {
		UUID myUserId = userPrincipal.getId();
		FollowDto followDto = followService.createFollow(myUserId,request.followeeId());
		return ResponseEntity.status(HttpStatus.CREATED).body(followDto);
	}

	@Operation(summary = "팔로우 요약 정보 조회", description = "특정 사용자의 팔로워/팔로잉 수, 맞팔 여부 등을 조회합니다.")
	@ApiResponse(responseCode = "200", description = "조회 성공")
	@GetMapping("/summary")
	public ResponseEntity<FollowSummaryDto> getFollowsSummary(
			@Parameter(description = "프로필 소유자의 사용자 ID") @RequestParam UUID userId,
			@Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal userPrincipal
	) {
		UUID myUserId = userPrincipal.getId();
		FollowSummaryDto followSummaryDtoList = followService.getFollowSummary(userId,myUserId);
		return ResponseEntity.status(HttpStatus.OK).body(followSummaryDtoList);
	}

	@Operation(summary = "팔로잉 목록 조회", description = "특정 사용자가 팔로우하는 사용자 목록을 조회합니다.")
	@ApiResponse(responseCode = "200", description = "조회 성공")
	@GetMapping("/followings")
	public ResponseEntity<FollowListResponse> getFollowings(
			@Parameter(description = "팔로우하는 사람의 ID (주체)") @RequestParam UUID followerId,
			@Parameter(description = "페이지네이션 커서") @RequestParam(required = false) String cursor,
			@Parameter(description = "기준 ID") @RequestParam(required = false) UUID idAfter,
			@Parameter(description = "조회할 개수") @RequestParam int limit,
			@Parameter(description = "이름 검색 키워드") @RequestParam(required = false) String nameLike,
			@Parameter(description = "정렬 기준") @RequestParam(required = false) String sortBy,
			@Parameter(description = "정렬 방향") @RequestParam(required = false) String sortDirection
	) {
		FollowListResponse followingList = followService.getFollowings(followerId,cursor,idAfter,limit,nameLike,sortBy,sortDirection);
		return ResponseEntity.status(HttpStatus.OK).body(followingList);
	}

	@Operation(summary = "팔로워 목록 조회", description = "특정 사용자를 팔로우하는 사용자 목록을 조회합니다.")
	@ApiResponse(responseCode = "200", description = "조회 성공")
	@GetMapping("/followers")
	public ResponseEntity<FollowListResponse> getFollowers(
			@Parameter(description = "팔로우 당하는 사람의 ID (대상)") @RequestParam UUID followeeId,
			@Parameter(description = "페이지네이션 커서") @RequestParam(required = false) String cursor,
			@Parameter(description = "기준 ID") @RequestParam(required = false) UUID idAfter,
			@Parameter(description = "조회할 개수") @RequestParam int limit,
			@Parameter(description = "이름 검색 키워드") @RequestParam(required = false) String nameLike,
			@Parameter(description = "정렬 기준") @RequestParam(required = false) String sortBy,
			@Parameter(description = "정렬 방향") @RequestParam(required = false) String sortDirection
	) {
		FollowListResponse followerList = followService.getFollowers(followeeId,cursor,idAfter,limit,nameLike,sortBy,sortDirection);
		return ResponseEntity.status(HttpStatus.OK).body(followerList);
	}

	@Operation(summary = "팔로우 취소 (언팔로우)", description = "팔로우 관계를 취소합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "204", description = "언팔로우 성공"),
			@ApiResponse(responseCode = "403", description = "자신의 팔로우만 취소할 수 있음"),
			@ApiResponse(responseCode = "404", description = "해당 팔로우 관계를 찾을 수 없음")
	})
	@DeleteMapping("/{followId}")
	public ResponseEntity<Void> cancelFollow(
			@Parameter(description = "취소할 팔로우 관계의 ID") @PathVariable UUID followId,
			@Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal userPrincipal
	) {
		UUID myUserId = userPrincipal.getId();
		followService.cancelFollow(followId, myUserId);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}
}