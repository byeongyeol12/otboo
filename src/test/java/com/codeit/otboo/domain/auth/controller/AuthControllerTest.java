package com.codeit.otboo.domain.auth.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.codeit.otboo.domain.auth.dto.request.LoginRequest;
import com.codeit.otboo.domain.auth.dto.response.LoginResponse;
import com.codeit.otboo.domain.auth.service.AuthService;
import com.codeit.otboo.domain.auth.service.JwtBlacklistService;
import com.codeit.otboo.domain.auth.service.TokenCacheService;
import com.codeit.otboo.domain.user.service.UserService;
import com.codeit.otboo.global.config.jwt.JwtAuthenticationFilter;
import com.codeit.otboo.global.config.jwt.JwtTokenProvider;
import com.codeit.otboo.global.config.security.UserPrincipal;
import com.codeit.otboo.global.enumType.Role;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = true)
@ContextConfiguration
@TestPropertySource(properties = {
	"jwt.refresh-token-validity-in-ms=604800000",
	"jwt.secret=MySuperSecretJwtKeyThatShouldBeLongEnough123",
	"jwt.expiration=600000"
})
@Import({JwtAuthenticationFilter.class, AuthControllerTest.MockBeans.class})
class AuthControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private AuthService authService;

	@MockBean
	private JwtBlacklistService jwtBlacklistService;

	@MockBean
	private JwtTokenProvider jwtTokenProvider;

	private final ObjectMapper objectMapper = new ObjectMapper();

	@BeforeEach
	void setUpSecurityContext() {
		Claims mockClaims = Jwts.claims();
		mockClaims.put("userId", "123e4567-e89b-12d3-a456-426614174000");
		mockClaims.put("email", "test@test.com");
		mockClaims.put("role", "USER");

		when(jwtTokenProvider.validateToken(any())).thenReturn(true);
		when(jwtTokenProvider.getClaims(any())).thenReturn(mockClaims);
		when(jwtTokenProvider.isBlacklisted(any())).thenReturn(false);

		UserPrincipal userPrincipal = new UserPrincipal(
			UUID.fromString("123e4567-e89b-12d3-a456-426614174000"),
			"test@test.com",
			null,
			Role.USER
		);

		UsernamePasswordAuthenticationToken authentication =
			new UsernamePasswordAuthenticationToken(userPrincipal, null, userPrincipal.getAuthorities());

		SecurityContextHolder.getContext().setAuthentication(authentication);
	}

	@AfterEach
	void clearSecurityContext() {
		SecurityContextHolder.clearContext();
	}

	@Test
	@DisplayName("로그인 성공 - 액세스 토큰과 쿠키 반환")
	void signIn_success() throws Exception {
		LoginRequest request = new LoginRequest("test@test.com", "1234");
		LoginResponse response = new LoginResponse("access-token", "refresh-token", Instant.now().plusSeconds(3600));

		when(authService.login(any())).thenReturn(response);

		mockMvc.perform(post("/api/auth/sign-in")
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(header().exists(HttpHeaders.SET_COOKIE))
			.andExpect(content().string("access-token"));
	}

	@Test
	@DisplayName("로그아웃 성공")
	void signOut_success() throws Exception {
		String token = "Bearer someValidAccessToken";

		when(jwtTokenProvider.resolveToken(any(HttpServletRequest.class))).thenReturn("someValidAccessToken");

		mockMvc.perform(post("/api/auth/sign-out")
				.header(HttpHeaders.AUTHORIZATION, token)
				.with(csrf()))
			.andExpect(status().isOk())
			.andExpect(content().string("로그아웃 성공"));
	}

	@Test
	@DisplayName("리프레시 토큰으로 액세스 토큰 재발급")
	void refreshAccessToken_success() throws Exception {
		String refreshToken = "refresh-token";
		String accessToken = "new-access-token";

		Cookie cookie = new Cookie("refreshToken", refreshToken);

		when(authService.refreshAccessToken(refreshToken)).thenReturn(accessToken);

		mockMvc.perform(post("/api/auth/refresh")
				.cookie(cookie)
				.with(csrf()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.accessToken").value(accessToken));
	}

	@Test
	@DisplayName("CSRF 토큰 반환")
	void getCsrfToken_success() throws Exception {
		mockMvc.perform(get("/api/auth/csrf-token").with(csrf()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.headerName").exists())
			.andExpect(jsonPath("$.parameterName").exists())
			.andExpect(jsonPath("$.token").exists());
	}

	@Test
	@DisplayName("리프레시 토큰 추출 실패 - 쿠키 없음")
	void refreshAccessToken_noCookies() throws Exception {
		mockMvc.perform(post("/api/auth/refresh")
				.with(csrf()))
			.andExpect(status().isUnauthorized());
	}

	@Test
	@DisplayName("리프레시 토큰 추출 실패 - 쿠키에 refreshToken 없음")
	void refreshAccessToken_cookieWithoutRefreshToken() throws Exception {
		Cookie dummyCookie = new Cookie("otherToken", "someValue");

		mockMvc.perform(post("/api/auth/refresh")
				.cookie(dummyCookie)
				.with(csrf()))
			.andExpect(status().isUnauthorized());
	}

	@TestConfiguration
	static class MockBeans {

		@Bean
		public JwtTokenProvider jwtTokenProvider() {
			return new JwtTokenProvider(
				"MySuperSecretJwtKeyThatShouldBeLongEnough123",
				600_000,
				604_800_000,
				mock(JwtBlacklistService.class)
			);
		}

		@Bean
		public JwtAuthenticationFilter jwtAuthenticationFilter(JwtTokenProvider provider) {
			return new JwtAuthenticationFilter(provider);
		}

		@Bean
		public AuthService authService() {
			return Mockito.mock(AuthService.class);
		}

		@Bean
		public UserService userService() {
			return Mockito.mock(UserService.class);
		}

		@Bean
		public JwtBlacklistService jwtBlacklistService() {
			return Mockito.mock(JwtBlacklistService.class);
		}

		@Bean
		public TokenCacheService tokenCacheService() {
			return Mockito.mock(TokenCacheService.class);
		}
	}
}
