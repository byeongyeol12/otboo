package com.codeit.otboo.domain.notification.controller;

import com.codeit.otboo.domain.notification.dto.NotificationDtoCursorResponse;
import com.codeit.otboo.domain.notification.service.NotificationService;
import com.codeit.otboo.global.config.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "알림", description = "사용자 알림 조회 및 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController {
	private final NotificationService notificationService;

	@Operation(summary = "알림 목록 조회", description = "로그인한 사용자의 알림 목록을 페이지네이션으로 조회합니다.")
	@ApiResponse(responseCode = "200", description = "조회 성공")
	@GetMapping
	public ResponseEntity<NotificationDtoCursorResponse> getNotifications(
			@Parameter(description = "페이지네이션 커서") @RequestParam(required = false) String cursor,
			@Parameter(description = "기준 ID") @RequestParam(required = false) UUID idAfter,
			@Parameter(description = "조회할 개수") @RequestParam int limit,
			@Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal userPrincipal
	) {
		UUID userId = userPrincipal.getId();
		NotificationDtoCursorResponse notificationDtoCursorResponse = notificationService.getNotifications(userId,cursor,idAfter,limit);
		return ResponseEntity.status(HttpStatus.OK).body(notificationDtoCursorResponse);
	}

	@Operation(summary = "알림 읽음 처리", description = "특정 알림을 읽음 상태로 변경합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "204", description = "읽음 처리 성공"),
			@ApiResponse(responseCode = "403", description = "자신의 알림만 읽을 수 있음"),
			@ApiResponse(responseCode = "404", description = "해당 알림을 찾을 수 없음")
	})
	@DeleteMapping("/{notificationId}")
	public ResponseEntity<Void> readNotifications(
			@Parameter(description = "읽음 처리할 알림의 ID") @PathVariable UUID notificationId,
			@Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal userPrincipal
	) {
		UUID userId = userPrincipal.getId();
		notificationService.readNotifications(notificationId,userId);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}
}