package com.codeit.otboo.domain.sse.controller;

import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.codeit.otboo.domain.sse.service.SseEmitterService;
import com.codeit.otboo.global.config.security.UserPrincipal;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sse")
@Tag(name = "SSE(실시간 알림)", description = "SSE(서버센트이벤트) 실시간 알림 구독/수신 API")
public class SseController {

	private final SseEmitterService sseEmitterService;

	@GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	@Operation(
			summary = "SSE 알림 구독",
			description = "실시간 알림을 구독합니다. LastEventId가 있으면 해당 시점 이후 알림부터 전송합니다."
	)
	public SseEmitter subscribe(
			@Parameter(hidden = true)
			@AuthenticationPrincipal UserPrincipal userPrincipal, // 인증된 유저 객체(문서화 제외)

			@Parameter(
					description = "이전에 수신하지 못한 마지막 알림의 ID(UUID). " +
							"해당 값이 있으면 누락된 알림부터 다시 전송합니다.",
					example = "bd4f46fd-dfd4-4b09-a57c-9aefb1cb38a1"
			)
			@RequestParam(value = "LastEventId", required = false)
			UUID lastEventId
	) {
		UUID userId = userPrincipal.getId();
		return sseEmitterService.subscribe(userId, lastEventId);
	}
}
