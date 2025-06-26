package com.codeit.otboo.domain.follow.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.codeit.otboo.domain.follow.dto.FollowCreateRequest;
import com.codeit.otboo.domain.follow.dto.FollowDto;
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
}
