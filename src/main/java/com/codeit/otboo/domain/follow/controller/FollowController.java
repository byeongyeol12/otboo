package com.codeit.otboo.domain.follow.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import com.codeit.otboo.domain.follow.dto.FollowSummaryDto;
import com.codeit.otboo.domain.follow.service.FollowService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/follows")
@RequiredArgsConstructor
public class FollowController {
	private final FollowService followService;

	//팔로우 생성
	@PostMapping
	public ResponseEntity<FollowDto> createFollow(@RequestBody FollowCreateRequest request) {
		FollowDto followDto = followService.createFollow(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(followDto);
	}

	//팔로우 요약 정보 조회
	@GetMapping("/summary")
	public ResponseEntity<List<FollowSummaryDto>> getFollowsSummary(
		@RequestParam UUID userId, // 대상 유저 id
		@RequestParam UUID myUserId // 내 id 추후 JWT 에서 추출
	) {
		List<FollowSummaryDto> followSummaryDtoList = followService.getFollowSummary(userId,myUserId);
		return ResponseEntity.status(HttpStatus.OK).body(followSummaryDtoList);
	}

	//팔로잉 목록 조회(내가 팔로우한 사람들)
	@GetMapping("/followings")
	public ResponseEntity<List<FollowDto>> getFollowings(
		@RequestParam UUID followerId,
		@RequestParam(required = false) String cursor,
		@RequestParam(required = false) UUID idAfter,
		@RequestParam int limit,
		@RequestParam(required = false) String nameLike
	) {
		List<FollowDto> followingList = followService.getFollowings(followerId,cursor,idAfter,limit,nameLike);
		return ResponseEntity.status(HttpStatus.OK).body(followingList);
	}
	//팔로워 목록 조회
	@GetMapping("/followers")
	public ResponseEntity<List<FollowDto>> getFollowers(
		@RequestParam UUID followeeId,
		@RequestParam(required = false) String cursor,
		@RequestParam(required = false) UUID idAfter,
		@RequestParam int limit,
		@RequestParam(required = false) String nameLike
	) {
		List<FollowDto> followerList = followService.getFollowers(followeeId,cursor,idAfter,limit,nameLike);
		return ResponseEntity.status(HttpStatus.OK).body(followerList);
	}

	//팔로우 취소
	@DeleteMapping("/{followId}")
	public ResponseEntity<Void> cancelFollow(@PathVariable UUID followId,@RequestParam UUID loginUserId) { // 추후 JWT 에서 추출
		followService.cancelFollow(followId, loginUserId);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}
}
