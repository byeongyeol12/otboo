package com.codeit.otboo.domain.sse.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.codeit.otboo.domain.notification.dto.NotificationDto;
import com.codeit.otboo.domain.notification.service.NotificationService;
import com.codeit.otboo.domain.sse.service.SseEmitterService;
import com.codeit.otboo.global.config.security.UserPrincipal;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sse")
public class SseController {
	private final SseEmitterService sseEmitterService;
	private final NotificationService notificationService;

	@GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public SseEmitter subscribe(
		@AuthenticationPrincipal UserPrincipal userPrincipal, // 로그인한 유저
		@RequestParam(value = "LastEventId",required = false) UUID lastEventId // 해당 사용자에 대한 미수신 알림을 보내주기 위해
	){
		UUID userId = userPrincipal.getId();
		List<NotificationDto> missed = notificationService.findUnreceived(userId, lastEventId);
		return sseEmitterService.subscribe(userId,missed);
	}
}
