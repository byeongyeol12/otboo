package com.codeit.otboo.domain.follow.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.codeit.otboo.domain.auth.service.JwtBlacklistService;
import com.codeit.otboo.domain.follow.dto.FollowCreateRequest;
import com.codeit.otboo.domain.follow.dto.FollowDto;
import com.codeit.otboo.domain.follow.service.FollowService;
import com.codeit.otboo.domain.user.dto.response.UserSummaryDto;
import com.codeit.otboo.domain.user.entity.User;
import com.codeit.otboo.global.config.jwt.JwtTokenProvider;
import com.codeit.otboo.global.config.security.UserPrincipal;
import com.codeit.otboo.global.enumType.Role;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(controllers = FollowController.class)
public class FollowControllerTest {
	@Autowired
	private MockMvc mockMvc; //컨트롤러 계층만 테스트

	@Autowired
	private ObjectMapper objectMapper; //JSON 직렬화,역직렬화

	@MockitoBean
	private FollowService followService;
	@MockitoBean
	private JwtTokenProvider jwtTokenProvider;
	@MockitoBean
	private JwtBlacklistService jwtBlacklistService;
	@MockitoBean
	private StringRedisTemplate stringRedisTemplate;

	// 테스트용 UserPrincipal 생성 함수
	private UserPrincipal userPrincipal() {
		return new UserPrincipal(
			UUID.fromString("11111111-1111-1111-1111-111111111111"),
			"test@email.com",
			"test",
			Role.USER
		);
	}

	private User follower;
	private User followee;
	private User user;

	private static final UUID followerId = UUID.fromString("00000000-0000-0000-0000-000000000001");
	private static final UUID followeeId = UUID.fromString("00000000-0000-0000-0000-000000000002");
	private static final UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000003");

	private static final UUID FOLLOW_ID_1 = UUID.fromString("00000000-0000-0000-0000-000000000004");
	private static final UUID FOLLOW_ID_2 = UUID.fromString("00000000-0000-0000-0000-000000000005");
	private static final UUID FOLLOW_ID_3 = UUID.fromString("00000000-0000-0000-0000-000000000006");

	@BeforeEach
	public void setUp() {
		// 팔로워(유저 , 팔로우를 거는 사람)
		follower = new User();
		follower.setId(followerId);
		follower.setName("팔로워");

		// 팔로이(팔로우 대상)
		followee = new User();
		followee.setId(followeeId);
		followee.setName("팔로이");

		User user = new User();
		user.setId(userId);
		user.setName("유저");
	}

	@Test
	@DisplayName("createFollow - 팔로우 생성 성공")
	@WithMockUser// 스프링 시큐리티 인증 적용
	void createFollow_success() throws Exception {
		//given
		FollowCreateRequest followCreateRequest = new FollowCreateRequest(
			followeeId, followerId
		);
		UUID myUserId = userPrincipal().getId();
		FollowDto dto = new FollowDto(
			UUID.randomUUID(),
			new UserSummaryDto(followee.getId(), followee.getName(), null),
			new UserSummaryDto(follower.getId(), follower.getName(), null)
		);
		when(followService.createFollow(eq(myUserId), eq(followCreateRequest.followeeId()))).thenReturn(dto);

		//when
		mockMvc.perform(post("/api/follows")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(followCreateRequest))
				.with(SecurityMockMvcRequestPostProcessors.user(userPrincipal()))
			)
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.id").exists());
	}
}
