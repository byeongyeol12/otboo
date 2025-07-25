package com.codeit.otboo.domain.dm.controller;

import com.codeit.otboo.domain.dm.dto.DirectMessageDtoCursorResponse;
import com.codeit.otboo.domain.dm.service.DmService;
import com.codeit.otboo.global.config.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "DM (다이렉트 메시지)", description = "사용자 간 1:1 메시지 API")
@RestController
@RequiredArgsConstructor
@Slf4j
public class DmController {
	private final DmService dmService;

	@Operation(summary = "DM 목록 조회", description = "특정 사용자와 주고받은 DM 목록을 페이지네이션으로 조회합니다.")
	@ApiResponse(responseCode = "200", description = "조회 성공")
	@GetMapping("/api/direct-messages")
	public ResponseEntity<DirectMessageDtoCursorResponse> getDms(
			@Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal userPrincipal,
			@Parameter(description = "대화 상대방의 사용자 ID", required = true) @RequestParam UUID userId,
			@Parameter(description = "페이지네이션 커서") @RequestParam(required = false) String cursor,
			@Parameter(description = "기준 ID") @RequestParam(required = false) UUID idAfter,
			@Parameter(description = "조회할 개수") @RequestParam int limit
	){
		UUID myUserId = userPrincipal.getId();
		DirectMessageDtoCursorResponse dmList = dmService.getDms(myUserId,userId,cursor,idAfter,limit);
		return ResponseEntity.status(HttpStatus.OK).body(dmList);
	}
}