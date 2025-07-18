package com.codeit.otboo.domain.dm.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.codeit.otboo.domain.dm.dto.DirectMessageDtoCursorResponse;
import com.codeit.otboo.domain.dm.service.DmService;
import com.codeit.otboo.global.config.security.UserPrincipal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@Slf4j
public class DmController {
	private final DmService dmService;

	//메시지 목록 조회
	@GetMapping("/api/direct-messages")
	public ResponseEntity<DirectMessageDtoCursorResponse> getDms(
		@AuthenticationPrincipal UserPrincipal userPrincipal,
		@RequestParam UUID userId, // 상대방 아이디
		@RequestParam(required = false) String cursor,
		@RequestParam(required = false) UUID idAfter,
		@RequestParam int limit
	){
		UUID myUserId = userPrincipal.getId(); // 인증된 사용자 ID 만 사용
		DirectMessageDtoCursorResponse dmList = dmService.getDms(myUserId,userId,cursor,idAfter,limit);
		return ResponseEntity.status(HttpStatus.OK).body(dmList);
	}
}
