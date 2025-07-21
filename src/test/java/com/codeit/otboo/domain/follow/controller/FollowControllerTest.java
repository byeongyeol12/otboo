package com.codeit.otboo.domain.follow.controller;

import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.codeit.otboo.domain.TestApplication;
import com.codeit.otboo.domain.follow.dto.FollowCreateRequest;
import com.codeit.otboo.domain.follow.dto.FollowDto;
import com.codeit.otboo.domain.follow.dto.FollowListResponse;
import com.codeit.otboo.domain.follow.dto.FollowSummaryDto;
import com.codeit.otboo.domain.follow.service.FollowService;
import com.codeit.otboo.domain.user.dto.response.UserSummaryDto;
import com.codeit.otboo.exception.CustomException;
import com.codeit.otboo.global.config.security.UserPrincipal;
import com.codeit.otboo.global.enumType.Role;
import com.codeit.otboo.global.error.ErrorCode;
import com.codeit.otboo.global.error.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(controllers = FollowController.class)
@ContextConfiguration(classes = TestApplication.class)
@Import(GlobalExceptionHandler.class)
class FollowControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private FollowService followService;

	//createFollow
	@Test
	@WithMockUser
	@DisplayName("createFollow - 팔로우 생성 성공")
	void createFollow_success() throws Exception {
		UUID followeeId = UUID.randomUUID();
		UUID userId = UUID.randomUUID();
		UserPrincipal userPrincipal = new UserPrincipal(userId, "test@email.com", "pw", Role.USER);

		FollowCreateRequest request = new FollowCreateRequest(followeeId, userPrincipal.getId());
		FollowDto responseDto = new FollowDto(
			UUID.randomUUID(),
			new UserSummaryDto(followeeId, "followeeName", null),
			new UserSummaryDto(userPrincipal.getId(), "followerName", null)
		);

		given(followService.createFollow(userPrincipal.getId(), followeeId))
			.willReturn(responseDto);

		mockMvc.perform(post("/api/follows")
				.with(SecurityMockMvcRequestPostProcessors.user(userPrincipal))
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isCreated())
			.andExpect(content().json(objectMapper.writeValueAsString(responseDto)));
	}

	@Test
	@WithMockUser
	@DisplayName("createFollow - 필드 누락으로 인한 실패")
	void createFollow_fail() throws Exception {
		//given
		UUID userId = UUID.randomUUID();
		UserPrincipal userPrincipal = new UserPrincipal(userId, "test@email.com", "pw", Role.USER);

		FollowCreateRequest request = new FollowCreateRequest(null, userPrincipal.getId());

		//when,then
		mockMvc.perform(post("/api/follows")
				.with(SecurityMockMvcRequestPostProcessors.user(userPrincipal))
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))
			)
			.andExpect(status().isInternalServerError());
	}

	@Test
	@WithMockUser
	@DisplayName("createFollow - 존재하지 않는 유저 팔로우 요청으로 실패")
	void createFollow_fail_wrongUser() throws Exception {
		//given
		UUID anotherUserId = UUID.randomUUID();
		UUID userId = UUID.randomUUID();
		UserPrincipal userPrincipal = new UserPrincipal(userId, "test@email.com", "pw", Role.USER);

		FollowCreateRequest request = new FollowCreateRequest(anotherUserId, userPrincipal.getId());

		when(followService.createFollow(eq(userPrincipal.getId()), eq(anotherUserId))).thenThrow(
			new CustomException(ErrorCode.USER_NOT_FOUND));

		//when,then
		mockMvc.perform(post("/api/follows")
				.with(SecurityMockMvcRequestPostProcessors.user(userPrincipal))
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))
			)
			.andExpect(status().isNotFound());
	}
	@Test
	@DisplayName("createFollow - 미인증 사용자는 401 반환 실패")
	void createFollow_unauthenticated() throws Exception {
		//given
		UUID followeeId = UUID.randomUUID();
		UUID userId = UUID.randomUUID();
		FollowCreateRequest req = new FollowCreateRequest(followeeId, userId);

		//when,then
		mockMvc.perform(post("/api/follows")
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(req)))
			.andExpect(status().isUnauthorized());
	}

	//getFollowsSummary
	@Test
	@WithMockUser
	@DisplayName("getFollowsSummary - 팔로우 요약 정보 성공")
	void getFollowsSummary_success() throws Exception {
		//given
		UUID userId = UUID.randomUUID();
		UUID myUserId = UUID.randomUUID();
		UserPrincipal userPrincipal = new UserPrincipal(myUserId, "test@email.com", "pw", Role.USER);

		FollowSummaryDto dto = new FollowSummaryDto(
			userId, 10L, 5L, true, UUID.randomUUID(), false
		);

		when(followService.getFollowSummary(eq(userId), eq(userPrincipal.getId()))).thenReturn(dto);

		//when,then
		mockMvc.perform(get("/api/follows/summary")
				.with(SecurityMockMvcRequestPostProcessors.user(userPrincipal))
				.param("userId", userId.toString())
			)
			.andExpect(status().isOk());
	}

	@Test
	@WithMockUser
	@DisplayName("getFollowsSummary - 필수 파라미터 누락으로 실패")
	void getFollowsSummary_fail() throws Exception {
		//given
		UUID myUserId = UUID.randomUUID();
		UserPrincipal userPrincipal = new UserPrincipal(myUserId, "test@email.com", "pw", Role.USER);

		//when,then
		mockMvc.perform(get("/api/follows/summary")
				.with(SecurityMockMvcRequestPostProcessors.user(userPrincipal))
			)
			.andExpect(status().isInternalServerError());
	}

	//getFollowings
	@Test
	@WithMockUser
	@DisplayName("getFollowings - 팔로잉 목록 성공")
	void getFollowing_success() throws Exception {
		//given
		UUID followerId = UUID.randomUUID();
		FollowDto followDto = new FollowDto(
			UUID.randomUUID(),
			new UserSummaryDto(UUID.randomUUID(), "팔로이", null),
			new UserSummaryDto(followerId, "팔로워", null)
		);
		FollowListResponse response = new FollowListResponse(
			List.of(followDto), null, null, false, 1L, null, null
		);
		when(followService.getFollowings(eq(followerId), any(), any(), anyInt(), any(), any(), any()))
			.thenReturn(response);

		//when,then
		mockMvc.perform(get("/api/follows/followings")
				.param("followerId", followerId.toString())
				.param("limit", "10"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data[0].follower.id").value(followerId.toString()));
	}
	@Test
	@WithMockUser
	@DisplayName("getFollowings - 팔로잉 목록 followerId 누락 시 실패")
	void getFollowings_paramMissing() throws Exception {
		//given
		UUID userId = UUID.randomUUID();
		UserPrincipal userPrincipal = new UserPrincipal(userId, "test@email.com", "pw", Role.USER);

		//when,then
		mockMvc.perform(get("/api/follows/followings")
				.param("limit", "10")
				.with(user(userPrincipal)))
			.andExpect(status().isInternalServerError());
	}

	//getFollowers
	@Test
	@WithMockUser
	@DisplayName("getFollowers - 팔로워 목록 성공")
	void getFollowers_success() throws Exception {
		//given
		UUID followeeId = UUID.randomUUID();
		FollowDto followDto = new FollowDto(
			UUID.randomUUID(),
			new UserSummaryDto(followeeId, "팔로이", null),
			new UserSummaryDto(UUID.randomUUID(), "팔로워", null)
		);
		FollowListResponse response = new FollowListResponse(
			List.of(followDto), null, null, false, 1L, null, null
		);
		when(followService.getFollowers(eq(followeeId), any(), any(), anyInt(), any(), any(), any()))
			.thenReturn(response);

		//when,then
		mockMvc.perform(get("/api/follows/followers")
				.param("followeeId", followeeId.toString())
				.param("limit", "10"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data[0].followee.id").value(followeeId.toString()));
	}
	@Test
	@WithMockUser
	@DisplayName("getFollowers - 팔로우 목록 followeeId 누락 시 실패")
	void getFollowers_paramMissing() throws Exception {
		//given
		UUID userId = UUID.randomUUID();
		UserPrincipal userPrincipal = new UserPrincipal(userId, "test@email.com", "pw", Role.USER);

		//when,then
		mockMvc.perform(get("/api/follows/followers")
				.param("limit", "10")
				.with(user(userPrincipal)))
			.andExpect(status().isInternalServerError());
	}

	//cancelFollow
	@Test
	@WithMockUser
	@DisplayName("cancelFollow - 팔로우 취소 성공")
	void cancelFollow_success() throws Exception {
		//given
		UUID followId = UUID.randomUUID();
		UUID myUserId = UUID.randomUUID();
		UserPrincipal userPrincipal = new UserPrincipal(myUserId, "test@email.com", "pw", Role.USER);

		willDoNothing().given(followService).cancelFollow(eq(followId), eq(userPrincipal.getId()));

		//when,then
		mockMvc.perform(delete("/api/follows/" + followId)
				.with(SecurityMockMvcRequestPostProcessors.user(userPrincipal))
				.with(csrf()))
			.andExpect(status().isNoContent());

	}

	@Test
	@WithMockUser
	@DisplayName("cancelFollow - 팔로우가 없어서 실패")
	void cancelFollow_fail() throws Exception {
		//given
		UUID followId = UUID.randomUUID();
		UUID myUserId = UUID.randomUUID();
		UserPrincipal userPrincipal = new UserPrincipal(myUserId, "test@email.com", "pw", Role.USER);

		willThrow(new CustomException(ErrorCode.FOLLOW_NOT_FOUND))
			.given(followService).cancelFollow(eq(followId), eq(userPrincipal.getId()));
		mockMvc.perform(delete("/api/follows/" + followId)
				.with(SecurityMockMvcRequestPostProcessors.user(userPrincipal))
				.with(csrf()))
			.andExpect(status().isNotFound());
	}

}
