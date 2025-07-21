package com.codeit.otboo.domain.notification.controller;

import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.codeit.otboo.domain.TestApplication;
import com.codeit.otboo.domain.notification.dto.NotificationDto;
import com.codeit.otboo.domain.notification.dto.NotificationDtoCursorResponse;
import com.codeit.otboo.domain.notification.entity.NotificationLevel;
import com.codeit.otboo.domain.notification.service.NotificationServiceImpl;
import com.codeit.otboo.exception.CustomException;
import com.codeit.otboo.global.config.security.UserPrincipal;
import com.codeit.otboo.global.enumType.Role;
import com.codeit.otboo.global.error.ErrorCode;
import com.codeit.otboo.global.error.GlobalExceptionHandler;

@WebMvcTest(controllers = NotificationController.class)
@ContextConfiguration(classes = TestApplication.class)
@Import(GlobalExceptionHandler.class)
public class NotificationControllerTest {
	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private NotificationServiceImpl notificationService;

	@Test
	@WithMockUser
	@DisplayName("getNotifications - 알림 목록 조회 성공")
	public void getNotifications_success() throws Exception {
		// given
		UUID userId = UUID.randomUUID();
		String cursor = "CURSOR_1";
		UUID idAfter = UUID.randomUUID();
		int limit = 10;

		NotificationDto notificationDto = new NotificationDto(
			UUID.randomUUID(), Instant.now(), userId, "테스트 알림", "content", NotificationLevel.INFO
		);

		List<NotificationDto> notificationList = List.of(notificationDto);

		NotificationDtoCursorResponse response = new NotificationDtoCursorResponse(
			notificationList,
			"CURSOR_NEXT",
			UUID.randomUUID(),
			true,
			30L,
			"createdAt",
			"DESC"
		);

		UserPrincipal principal = new UserPrincipal(userId, "test@email.com", "pw", Role.USER);

		when(notificationService.getNotifications(eq(userId), eq(cursor), eq(idAfter), eq(limit)))
			.thenReturn(response);

		// when & then
		mockMvc.perform(get("/api/notifications")
				.param("cursor", cursor)
				.param("idAfter", idAfter.toString())
				.param("limit", String.valueOf(limit))
				.with(SecurityMockMvcRequestPostProcessors.user(principal)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data[0].title").value("테스트 알림"))      // 알림 제목
			.andExpect(jsonPath("$.nextCursor").value("CURSOR_NEXT"))        // 다음 커서
			.andExpect(jsonPath("$.hasNext").value(true))                    // hasNext
			.andExpect(jsonPath("$.totalCount").value(30))                   // 전체 개수
			.andExpect(jsonPath("$.sortBy").value("createdAt"))              // 정렬 기준
			.andExpect(jsonPath("$.sortDirection").value("DESC"));

		verify(notificationService, times(1)).getNotifications(userId, cursor, idAfter, limit);
	}

	@Test
	@WithMockUser
	@DisplayName("getNotifications - 필수 파라미터 누락으로 실패")
	public void getNotifications_fail() throws Exception {
		//given

		//when,then
		mockMvc.perform(get("/api/notifications"))
			.andExpect(status().isBadRequest());
	}

	//readNotifications
	@Test
	@WithMockUser
	@DisplayName("readNotifications - 알림 읽음 성공")
	public void readNotifications_success() throws Exception {
		//given
		UUID notificationId = UUID.randomUUID();
		UUID userId = UUID.randomUUID();
		UserPrincipal principal = new UserPrincipal(userId, "test@email.com", "pw", Role.USER);

		doNothing().when(notificationService).readNotifications(notificationId, principal.getId());

		//when,then
		mockMvc.perform(delete("/api/notifications/" + notificationId)
				.with(SecurityMockMvcRequestPostProcessors.user(principal))
				.with(csrf()))
			.andExpect(status().isNoContent());
		verify(notificationService, times(1)).readNotifications(notificationId, userId);
	}

	@Test
	@WithMockUser
	@DisplayName("readNotifications - 존재하지 않는 알림으로 실패")
	public void readNotifications_fail() throws Exception {
		// given
		UUID notificationId = UUID.randomUUID();
		UUID userId = UUID.randomUUID();
		UserPrincipal principal = new UserPrincipal(userId, "test@email.com", "pw", Role.USER);

		// CustomException으로 예외 던지도록 목 세팅
		doThrow(new CustomException(ErrorCode.NOTIFICATION_NOT_FOUND))
			.when(notificationService).readNotifications(eq(notificationId), eq(userId));

		mockMvc.perform(delete("/api/notifications/{notificationId}", notificationId)
				.with(SecurityMockMvcRequestPostProcessors.user(principal))
				.with(csrf()))
			.andExpect(status().isNotFound()) // ErrorCode.NOTIFICATION_NOT_FOUND.getStatus() == 404
			.andExpect(jsonPath("$.message").value("알림 정보를 찾을 수 없습니다.")); // ErrorCode의 message 값
	}
}
