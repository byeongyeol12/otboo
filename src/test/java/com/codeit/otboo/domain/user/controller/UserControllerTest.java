package com.codeit.otboo.domain.user.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.security.Principal;
import java.time.Instant;
import java.time.LocalDate;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import com.codeit.otboo.domain.location.dto.response.LocationResponse;
import com.codeit.otboo.domain.user.dto.request.PasswordRequest;
import com.codeit.otboo.domain.user.dto.request.ProfileUpdateRequest;
import com.codeit.otboo.domain.user.dto.request.UserCreateRequest;
import com.codeit.otboo.domain.user.dto.request.UserLockRequest;
import com.codeit.otboo.domain.user.dto.request.UserRoleUpdateRequest;
import com.codeit.otboo.domain.user.dto.response.LocationDto;
import com.codeit.otboo.domain.user.dto.response.ProfileDto;
import com.codeit.otboo.domain.user.dto.response.UserDto;
import com.codeit.otboo.domain.user.dto.response.UserDtoCursorResponse;
import com.codeit.otboo.domain.user.entity.User;
import com.codeit.otboo.domain.user.service.UserService;
import com.codeit.otboo.global.enumType.Gender;
import com.codeit.otboo.global.enumType.Role;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@WebMvcTest(UserController.class)
@ContextConfiguration
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
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.name").value("홍길동"))
			.andExpect(jsonPath("$.email").value("test@test.com"));
	}

	@Test
	@DisplayName("유저 목록 조회 성공")
	@WithMockUser(roles = "ADMIN")
	void getAllUsers_success() throws Exception {
		UserDtoCursorResponse response = new UserDtoCursorResponse(
			List.of(), null, null, false, 0L, "createdAt", "desc");

		when(userService.searchUsers(any())).thenReturn(response);

		mockMvc.perform(get("/api/users"))
			.andExpect(status().isOk());
	}

	@Test
	@DisplayName("비밀번호 변경 성공")
	@WithMockUser
	void changePassword_success() throws Exception {
		UUID userId = UUID.randomUUID();
		PasswordRequest request = new PasswordRequest("newPass123!");

		Principal principal = mock(Principal.class);

		doNothing().when(userService).changePassword(eq(userId), any(), any());

		mockMvc.perform(patch("/api/users/" + userId + "/password")
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))
				.principal(principal))
			.andExpect(status().isNoContent());
	}

	@Test
	@DisplayName("권한 수정 성공")
	@WithMockUser(roles = "ADMIN")
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
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(userId.toString()));
	}

	@Test
	@DisplayName("계정 잠금 상태 수정 성공")
	@WithMockUser(roles = "ADMIN")
	void updateUserLock_success() throws Exception {
		UUID userId = UUID.randomUUID();
		User user = new User();
		user.setId(userId);
		user.setEmail("test@test.com");
		user.setName("홍길동");
		user.setPasswordHash("encoded-pw");
		user.setRole(Role.USER);
		user.setLocked(false);

		UserLockRequest request = new UserLockRequest(true);

		when(userService.updateUserLock(eq(userId), eq(true), anyString())).thenReturn(user);

		mockMvc.perform(patch("/api/users/" + userId + "/lock")
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.header("Authorization", "Bearer token")
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.userId").value(userId.toString()));
	}

	@Test
	@DisplayName("프로필 조회 성공")
	@WithMockUser
	void getProfile_success() throws Exception {
		UUID userId = UUID.randomUUID();
		LocationDto locationDto = new LocationDto(37.5, 127.0, 60, 127, List.of("서울특별시", "강남구"));
		ProfileDto dto = new ProfileDto(userId, "홍길동", Gender.MALE, LocalDate.of(2000, 1, 1), locationDto, 3,
			"http://image.com/profile.jpg");

		when(userService.getProfile(eq(userId))).thenReturn(dto);

		mockMvc.perform(get("/api/users/" + userId + "/profiles"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.userId").value(userId.toString()))
			.andExpect(jsonPath("$.name").value("홍길동"));
	}

	@Test
	@DisplayName("프로필 수정 성공")
	@WithMockUser
	void updateProfile_success() throws Exception {
		UUID userId = UUID.randomUUID();

		ObjectMapper mapper = new ObjectMapper()
			.registerModule(new JavaTimeModule())
			.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

		LocationResponse location = new LocationResponse(37.5, 127.0, 60, 127, List.of("서울특별시", "강남구"));
		ProfileUpdateRequest updateRequest = new ProfileUpdateRequest(
			"newNick", Gender.FEMALE, LocalDate.of(2000, 1, 1), location, 4
		);

		MockMultipartFile image = new MockMultipartFile(
			"image", "profile.jpg", MediaType.IMAGE_JPEG_VALUE, "fake-image".getBytes()
		);

		MockMultipartFile requestPart = new MockMultipartFile(
			"request", "", MediaType.APPLICATION_JSON_VALUE, mapper.writeValueAsBytes(updateRequest)
		);

		LocationDto locationDto = new LocationDto(37.5, 127.0, 60, 127, List.of("서울특별시", "강남구"));
		ProfileDto response = new ProfileDto(
			userId, "newNick", Gender.FEMALE, LocalDate.of(2000, 1, 1), locationDto, 4,
			"http://image.url/updated.jpg"
		);

		when(userService.updateProfile(eq(userId), any(), any())).thenReturn(response);

		mockMvc.perform(multipart("/api/users/{userId}/profiles", userId)
				.file(requestPart)
				.file(image)
				.with(csrf())
				.with(request -> {
					request.setMethod("PATCH"); // ⚠️ multipart는 기본적으로 POST이므로 PATCH로 수동 설정
					return request;
				})
				.contentType(MediaType.MULTIPART_FORM_DATA))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.name").value("newNick"))
			.andExpect(jsonPath("$.temperatureSensitivity").value(4));
	}

	@TestConfiguration
	static class MockConfig {
		@Bean
		public UserService userService() {
			return Mockito.mock(UserService.class);
		}
	}
}
