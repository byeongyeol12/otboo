package com.codeit.otboo.domain.dm.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import com.codeit.otboo.domain.auth.service.JwtBlacklistService;
import com.codeit.otboo.domain.dm.dto.DirectMessageDtoCursorResponse;
import com.codeit.otboo.domain.dm.service.DmService;
import com.codeit.otboo.global.config.jwt.JwtTokenProvider;
import com.codeit.otboo.global.config.security.UserPrincipal;
import com.codeit.otboo.global.enumType.Role;
import com.codeit.otboo.global.error.GlobalExceptionHandler;

@WebMvcTest(controllers = DmController.class)
@Import(GlobalExceptionHandler.class)
public class DmControllerTest {
	@Autowired
	private MockMvc mockMvc;
	@MockBean
	private DmService dmService;
	@MockBean
	private JwtTokenProvider jwtTokenProvider;
	@MockBean
	private JwtBlacklistService jwtBlacklistService;
	//getDms
	@Test
	@WithMockUser
	@DisplayName("getDms - DM 목록 조회(cursor X, idAfter X)")
	void getDms_noCursorIdAfter() throws Exception {
		//given
		DirectMessageDtoCursorResponse response = new DirectMessageDtoCursorResponse(
			List.of(),null,null,false,0L,null,null
		);
		UserPrincipal userPrincipal = new UserPrincipal(UUID.randomUUID(), "test@email.com", "pw", Role.USER);
		UUID myUserId = userPrincipal.getId();

		UUID otherUserId = UUID.randomUUID();
		when(dmService.getDms(eq(myUserId),eq(otherUserId),isNull(),isNull(),eq(20)))
			.thenReturn(response);
		//when,then
		mockMvc.perform(get("/api/direct-messages")
			.param("userId",otherUserId.toString())
			.param("limit","20")
			.with(SecurityMockMvcRequestPostProcessors.user(userPrincipal))
		)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data").exists())
			.andExpect(jsonPath("$.hasNext").value(false));
	}

	@Test
	@WithMockUser
	@DisplayName("getDms - DM 목록 조회(cursor O, idAfter O)")
	void getDms_usedCursorIdAfter() throws Exception {
		//given
		String cursor = "CURSOR";
		UUID idAfter = UUID.randomUUID();
		DirectMessageDtoCursorResponse response = new DirectMessageDtoCursorResponse(
			List.of(),cursor,idAfter,true,0L,null,null
		);
		UserPrincipal userPrincipal = new UserPrincipal(UUID.randomUUID(), "test@email.com", "pw", Role.USER);
		UUID myUserId = userPrincipal.getId();
		UUID otherUserId = UUID.randomUUID();

		when(dmService.getDms(eq(myUserId),eq(otherUserId),eq(cursor),eq(idAfter),eq(20)))
			.thenReturn(response);

		//when,then
		mockMvc.perform(get("/api/direct-messages")
				.param("userId", otherUserId.toString())
				.param("cursor", cursor)
				.param("idAfter", idAfter.toString())
				.param("limit", "20")
				.with(SecurityMockMvcRequestPostProcessors.user(userPrincipal))
			)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.hasNext").value(true))
			.andExpect(jsonPath("$.nextCursor").value("CURSOR"));
	}

	@Test
	@DisplayName("getDms - 인증 안 된 사용자 접근으로 실패")
	void getDms_unauthorized_failed() throws Exception {
		//given
		UserPrincipal userPrincipal = new UserPrincipal(UUID.randomUUID(), "test@email.com", "pw", Role.USER);
		UUID myUserId = userPrincipal.getId();
		UUID otherUserId = UUID.randomUUID();

		//when,then
		mockMvc.perform(get("/api/direct-messages")
			.param("userId", otherUserId.toString())
			.param("limit", "20")
		)
			.andExpect(status().isUnauthorized());
	}

	@Test
	@DisplayName("getDms - 필수 파라미터 누락으로 실패")
	@WithMockUser(username = "testuser")
	void getDms_missingParam_failed() throws Exception {
		//given
		UserPrincipal userPrincipal = new UserPrincipal(UUID.randomUUID(), "test@email.com", "pw", Role.USER);
		UUID myUserId = userPrincipal.getId();

		//when,then
		mockMvc.perform(get("/api/direct-messages")
				.param("limit", "20")
				.with(SecurityMockMvcRequestPostProcessors.user(userPrincipal))
			)
			.andExpect(status().isInternalServerError());
	}
}
