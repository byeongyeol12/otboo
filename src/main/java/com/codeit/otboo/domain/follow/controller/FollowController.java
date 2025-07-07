package com.codeit.otboo.domain.follow.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.codeit.otboo.domain.follow.dto.FollowCreateRequest;
import com.codeit.otboo.domain.follow.dto.FollowDto;
import com.codeit.otboo.domain.follow.dto.FollowListResponse;
import com.codeit.otboo.domain.follow.dto.FollowSummaryDto;
import com.codeit.otboo.domain.follow.service.FollowService;
import com.codeit.otboo.global.config.security.UserPrincipal;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/follows")
@RequiredArgsConstructor
public class FollowController {
	private final FollowService followService;

	//팔로우 생성(
	@PostMapping
	public ResponseEntity<FollowDto> createFollow(
		@Valid @RequestBody FollowCreateRequest request, // 유저가 팔로우한 사람(팔로이)
		@AuthenticationPrincipal UserPrincipal userPrincipal // 로그인한 유저(팔로워)
	) {
		UUID myUserId = userPrincipal.getId();
		FollowDto followDto = followService.createFollow(myUserId,request.followeeId());
		return ResponseEntity.status(HttpStatus.CREATED).body(followDto);
	}

	//팔로우 요약 정보 조회
	@GetMapping("/summary")
	public ResponseEntity<FollowSummaryDto> getFollowsSummary(
		@RequestParam UUID userId, // 팔로우 요약 정보를 가져오는 대상
		@AuthenticationPrincipal UserPrincipal userPrincipal // 로그인한 유저
	) {
		UUID myUserId = userPrincipal.getId();
		FollowSummaryDto followSummaryDtoList = followService.getFollowSummary(userId,myUserId);
		return ResponseEntity.status(HttpStatus.OK).body(followSummaryDtoList);
	}

	//팔로잉 목록 조회(유저가 팔로우한 사람들)
	@GetMapping("/followings")
	public ResponseEntity<FollowListResponse> getFollowings(
		@AuthenticationPrincipal UserPrincipal userPrincipal, // 로그인한 유저
		@RequestParam(required = false) String cursor,
		@RequestParam(required = false) UUID idAfter,
		@RequestParam int limit,
		@RequestParam(required = false) String nameLike,
		@RequestParam(required = false) String sortBy,
		@RequestParam(required = false) String sortDirection
	) {
		UUID myUserId = userPrincipal.getId();
		FollowListResponse followingList = followService.getFollowings(myUserId,cursor,idAfter,limit,nameLike,sortBy,sortDirection);
		return ResponseEntity.status(HttpStatus.OK).body(followingList);
	}

	//팔로워 목록 조회(유저를 팔로우한 사람들)
	@GetMapping("/followers")
	public ResponseEntity<FollowListResponse> getFollowers(
		@AuthenticationPrincipal UserPrincipal userPrincipal, // 로그인한 유저
		@RequestParam(required = false) String cursor,
		@RequestParam(required = false) UUID idAfter,
		@RequestParam int limit,
		@RequestParam(required = false) String nameLike,
		@RequestParam(required = false) String sortBy,
		@RequestParam(required = false) String sortDirection
	) {
		UUID myUserId = userPrincipal.getId();
		FollowListResponse followerList = followService.getFollowers(myUserId,cursor,idAfter,limit,nameLike,sortBy,sortDirection);
		return ResponseEntity.status(HttpStatus.OK).body(followerList);
	}

	//팔로우 취소
	@DeleteMapping("/{followId}")
	public ResponseEntity<Void> cancelFollow(
		@PathVariable UUID followId,
		@AuthenticationPrincipal UserPrincipal userPrincipal  // 로그인한 유저
	) {
		UUID myUserId = userPrincipal.getId();
		followService.cancelFollow(followId, myUserId);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}
}
