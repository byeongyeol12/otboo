package com.codeit.otboo.domain.dm.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.codeit.otboo.domain.dm.dto.DirectMessageDtoCursorResponse;
import com.codeit.otboo.domain.dm.service.DmServiceImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@Slf4j
public class DmController {
	private final DmServiceImpl dmService;

	//메시지 목록 조회
	@GetMapping("/api/direct-messages")
	public ResponseEntity<DirectMessageDtoCursorResponse> getDms(
		@RequestParam UUID userId,
		@RequestParam String cursor,
		@RequestParam UUID idAfter,
		@RequestParam int limit
	){
		return ResponseEntity.status(HttpStatus.OK).body();
	}

}
