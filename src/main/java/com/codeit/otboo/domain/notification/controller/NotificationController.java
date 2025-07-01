package com.codeit.otboo.domain.notification.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.codeit.otboo.domain.notification.dto.NotificationDtoCursorResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController {
	private final NotificationService notificationService;

	//알림 목록 조회
	@GetMapping
	public ResponseEntity<NotificationDtoCursorResponse> getNotifications(
		@RequestParam(required = false) String cursor,
		@RequestParam(required = false) String idAfter,
		int limit,
		@RequestParam(required = false) UUID userId // 추후 인증 토큰에서 추출

	) {
		NotificationDtoCursorResponse notificationDtoCursorResponse = notificationService.getNotifications(userId,cursor,idAfter,limit);
		return ResponseEntity.status(HttpStatus.OK).body(notificationDtoCursorResponse);
	}

	//알림 읽음 처리
}
