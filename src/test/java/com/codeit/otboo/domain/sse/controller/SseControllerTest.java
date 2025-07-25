package com.codeit.otboo.domain.sse.controller;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.codeit.otboo.domain.sse.service.SseEmitterService;
import com.codeit.otboo.global.config.security.UserPrincipal;
import com.codeit.otboo.global.enumType.Role;
import com.codeit.otboo.global.error.GlobalExceptionHandler;

@WebMvcTest(controllers = SseController.class)
@Import(GlobalExceptionHandler.class)
public class SseControllerTest {
	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private SseEmitterService sseEmitterService;

	@Test
	@WithMockUser
	@DisplayName("subscribe - sse 구독 성공(lastEventId X)")
	void subscribe_success() throws Exception {
		//given
		UUID userId = UUID.randomUUID();
		UUID lastEventId = null;
		UserPrincipal principal = new UserPrincipal(userId, "test@email.com", "pw", Role.USER);

		SseEmitter sseEmitter = new SseEmitter();
		when(sseEmitterService.subscribe(eq(principal.getId()), isNull())).thenReturn(sseEmitter);

		//when,then
		mockMvc.perform(get("/api/sse")
				.with(SecurityMockMvcRequestPostProcessors.user(principal))
				.accept(MediaType.TEXT_EVENT_STREAM_VALUE))
			.andExpect(status().isOk());
		verify(sseEmitterService).subscribe(eq(principal.getId()), isNull());
	}

	@Test
	@WithMockUser
	@DisplayName("subscribe - sse 구독 성공(lastEventId O)")
	void subscribe_success_with_lastEventId() throws Exception {
		//given
		UUID userId = UUID.randomUUID();
		UUID lastEventId = UUID.randomUUID();
		UserPrincipal principal = new UserPrincipal(userId, "test@email.com", "pw", Role.USER);

		SseEmitter sseEmitter = new SseEmitter();
		when(sseEmitterService.subscribe(eq(principal.getId()), eq(lastEventId))).thenReturn(sseEmitter);

		//when,then
		mockMvc.perform(get("/api/sse")
				.with(SecurityMockMvcRequestPostProcessors.user(principal))
				.accept(MediaType.TEXT_EVENT_STREAM_VALUE)
				.param("LastEventId", lastEventId.toString()))
			.andExpect(status().isOk());
		verify(sseEmitterService).subscribe(eq(principal.getId()), eq(lastEventId));
	}

	@Test
	@WithMockUser
	@DisplayName("subscribe - 인증 정보 없을 때 에러")
	void subscribe_fail_no_auth() throws Exception {
		//given

		//when,then
		mockMvc.perform(get("/api/sse"))
			.andExpect(status().isInternalServerError());
	}
}
