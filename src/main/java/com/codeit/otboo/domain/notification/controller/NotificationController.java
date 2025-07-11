package com.codeit.otboo.domain.notification.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.codeit.otboo.domain.notification.dto.NotificationDtoCursorResponse;
import com.codeit.otboo.domain.notification.service.NotificationService;
import com.codeit.otboo.global.config.security.UserPrincipal;

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
		@RequestParam(required = false) UUID idAfter,
		@RequestParam int limit,
		@AuthenticationPrincipal UserPrincipal userPrincipal
		) {
		UUID userId = userPrincipal.getId();
		NotificationDtoCursorResponse notificationDtoCursorResponse = notificationService.getNotifications(userId,cursor,idAfter,limit);
		return ResponseEntity.status(HttpStatus.OK).body(notificationDtoCursorResponse);
	}

	//알림 읽음 처리
	@DeleteMapping("/{notificationId}")
	public ResponseEntity<Void> readNotifications(
		@PathVariable UUID notificationId,
		@AuthenticationPrincipal UserPrincipal userPrincipal
	) {
		UUID userId = userPrincipal.getId();
		notificationService.readNotifications(notificationId,userId);

		return ResponseEntity.noContent().build();
	}
}
