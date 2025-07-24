package com.codeit.otboo.domain.user.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import com.codeit.otboo.TestApplication;
import com.codeit.otboo.domain.user.dto.request.UserCreateRequest;
import com.codeit.otboo.domain.user.dto.request.UserLockRequest;
import com.codeit.otboo.domain.user.dto.request.UserRoleUpdateRequest;
import com.codeit.otboo.domain.user.dto.response.UserDto;
import com.codeit.otboo.domain.user.dto.response.UserDtoCursorResponse;
import com.codeit.otboo.domain.user.entity.User;
import com.codeit.otboo.domain.user.service.UserService;
import com.codeit.otboo.global.enumType.Role;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(UserController.class)
@ContextConfiguration(classes = TestApplication.class)
@Import(UserControllerTest.MockConfig.class)
class UserControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserService userService;

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Test
	@DisplayName("회원가입 성공")
	@WithMockUser
		// 🔧 수정됨
	void signup_success() throws Exception {
		UserCreateRequest request = new UserCreateRequest("홍길동", "test@test.com", "Password123!");
		UserDto userDto = new UserDto(
			UUID.randomUUID(),
			Instant.now(),
			"test@test.com",
			"홍길동",
			Role.USER,
			List.of("USER"),
			false
		);

		when(userService.create(any())).thenReturn(userDto);

		mockMvc.perform(post("/api/users")
				.with(csrf()) // 🔧 수정됨
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.name").value("홍길동"))
			.andExpect(jsonPath("$.email").value("test@test.com"));
	}

	@Test
	@DisplayName("유저 목록 조회 성공")
	@WithMockUser(roles = "ADMIN")
		// 🔧 수정됨
	void getAllUsers_success() throws Exception {
		UserDtoCursorResponse response = new UserDtoCursorResponse(
			List.of(),
			null,
			null,
			false,
			0L,
			"createdAt",
			"desc"
		);

		when(userService.searchUsers(any())).thenReturn(response);

		mockMvc.perform(get("/api/users"))
			.andExpect(status().isOk());
	}

	@Test
	@DisplayName("권한 수정 성공")
	@WithMockUser(roles = "ADMIN")
		// 🔧 수정됨
	void updateUserRole_success() throws Exception {
		UUID userId = UUID.randomUUID();
		UserRoleUpdateRequest request = new UserRoleUpdateRequest(Role.ADMIN);
		UserDto updatedUser = new UserDto(
			userId,
			Instant.now(),
			"홍길동",
			"test@test.com",
			Role.ADMIN,
			List.of("ADMIN"),
			false
		);

		when(userService.updateUserRole(eq(userId), any())).thenReturn(updatedUser);

		mockMvc.perform(patch("/api/users/" + userId + "/role")
				.with(csrf()) // 🔧 수정됨
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(userId.toString()));
	}

	@Test
	@DisplayName("계정 잠금 상태 수정 성공")
	@WithMockUser(roles = "ADMIN")
		// 🔧 수정됨
	void updateUserLock_success() throws Exception {
		UUID userId = UUID.randomUUID();
		User user = new User(); // 기본 생성자 사용

		user.setId(userId);
		user.setEmail("test@test.com");
		user.setName("홍길동");
		user.setPasswordHash("encoded-pw");
		user.setRole(Role.USER);
		user.setLocked(false);

		UserLockRequest request = new UserLockRequest(true);

		when(userService.updateUserLock(eq(userId), eq(true), anyString())).thenReturn(user);

		mockMvc.perform(patch("/api/users/" + userId + "/lock")
				.with(csrf()) // 🔧 수정됨
				.contentType(MediaType.APPLICATION_JSON)
				.header("Authorization", "Bearer token")
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.userId").value(userId.toString()));
	}

	@TestConfiguration
	static class MockConfig {
		@Bean
		public UserService userService() {
			return Mockito.mock(UserService.class);
		}
	}
}
