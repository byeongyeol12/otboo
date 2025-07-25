package com.codeit.otboo.domain.auth.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.codeit.otboo.domain.auth.dto.request.LoginRequest;
import com.codeit.otboo.domain.auth.dto.response.LoginResponse;
import com.codeit.otboo.domain.user.entity.User;
import com.codeit.otboo.domain.user.repository.UserRepository;
import com.codeit.otboo.exception.CustomException;
import com.codeit.otboo.global.config.jwt.JwtTokenProvider;
import com.codeit.otboo.global.enumType.Role;
import com.codeit.otboo.global.error.ErrorCode;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

	@InjectMocks
	private AuthService authService;

	@Mock
	private UserRepository userRepository;
	@Mock
	private PasswordEncoder passwordEncoder;
	@Mock
	private JwtTokenProvider jwtTokenProvider;
	@Mock
	private TokenCacheService tokenCacheService;

	private User user;
	private final String email = "test@ootd.com";
	private final String rawPassword = "pw1234";
	private final String hashedPassword = "hashed_pw1234";

	@BeforeEach
	void setup() {
		user = new User();
		user.setId(UUID.randomUUID());
		user.setEmail(email);
		user.setName("테스터");
		user.setPasswordHash(hashedPassword);
		user.setRole(Role.USER);
		user.setLocked(false);
	}

	@Test
	@DisplayName("로그인 성공")
	void login_success() {
		LoginRequest request = new LoginRequest(email, rawPassword);

		given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
		given(passwordEncoder.matches(rawPassword, hashedPassword)).willReturn(true);
		given(jwtTokenProvider.generateToken(any(), any(), any(), any())).willReturn("access-token");
		given(jwtTokenProvider.generateRefreshToken(any())).willReturn("refresh-token");
		given(jwtTokenProvider.getTokenValidityInMilliseconds()).willReturn(3600000L);

		LoginResponse response = authService.login(request);

		assertThat(response.getAccessToken()).isEqualTo("access-token");
		assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
	}

	@Test
	@DisplayName("로그인 실패 - 이메일 없음")
	void login_emailNotFound() {
		LoginRequest request = new LoginRequest("wrong@ootd.com", rawPassword);
		given(userRepository.findByEmail(request.getEmail())).willReturn(Optional.empty());

		assertThatThrownBy(() -> authService.login(request))
			.isInstanceOf(CustomException.class)
			.hasMessageContaining(ErrorCode.USER_NOT_FOUND.getMessage());
	}

	@Test
	@DisplayName("로그인 실패 - 비밀번호 불일치")
	void login_passwordMismatch() {
		LoginRequest request = new LoginRequest(email, "wrong-password");
		given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
		given(passwordEncoder.matches(any(), any())).willReturn(false);

		assertThatThrownBy(() -> authService.login(request))
			.isInstanceOf(CustomException.class)
			.hasMessageContaining(ErrorCode.PASSWORD_MISMATCH.getMessage());
	}

	@Test
	@DisplayName("로그인 실패 - 계정 잠김")
	void login_lockedUser() {
		user.setLocked(true);
		LoginRequest request = new LoginRequest(email, rawPassword);

		given(userRepository.findByEmail(email)).willReturn(Optional.of(user));

		assertThatThrownBy(() -> authService.login(request))
			.isInstanceOf(CustomException.class)
			.hasMessageContaining(ErrorCode.FORBIDDEN.getMessage());
	}

	@Test
	@DisplayName("로그인 시 기존 리프레시 토큰 삭제 후 새로운 토큰 저장")
	void login_refreshTokenEvictedAndSaved() {
		LoginRequest request = new LoginRequest(email, rawPassword);

		given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
		given(passwordEncoder.matches(rawPassword, hashedPassword)).willReturn(true);
		given(jwtTokenProvider.generateToken(any(), any(), any(), any())).willReturn("access-token");
		given(jwtTokenProvider.generateRefreshToken(any())).willReturn("refresh-token");
		given(jwtTokenProvider.getTokenValidityInMilliseconds()).willReturn(3600000L);

		authService.login(request);

		verify(tokenCacheService).invalidateRefreshToken(user.getId());  // 기존 토큰 삭제 확인
		verify(tokenCacheService).saveRefreshToken(eq(user.getId()), eq("refresh-token")); // 새 토큰 저장 확인
	}

	@Test
	@DisplayName("로그아웃 성공 - 토큰 유효성 검사만 수행")
	void logout_success() {
		String accessToken = "valid-token";
		given(jwtTokenProvider.validateToken(accessToken)).willReturn(true);

		authService.logout(accessToken);

		verify(jwtTokenProvider).validateToken(accessToken);  // 검증만 호출됨
	}

	@Test
	@DisplayName("리프레시 토큰으로 액세스 토큰 재발급 성공")
	void refreshAccessToken_success() {
		String refreshToken = "refresh-token";
		String userId = user.getId().toString();

		given(jwtTokenProvider.validateToken(refreshToken)).willReturn(true);
		given(jwtTokenProvider.getSubject(refreshToken)).willReturn(userId);
		given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
		given(jwtTokenProvider.generateToken(any(), any(), any(), any())).willReturn("new-access-token");

		String result = authService.refreshAccessToken(refreshToken);

		assertThat(result).isEqualTo("new-access-token");
	}

	@Test
	@DisplayName("리프레시 토큰 재발급 실패 - 토큰 유효하지 않음")
	void refreshAccessToken_invalidToken() {
		String refreshToken = "invalid-refresh-token";

		given(jwtTokenProvider.validateToken(refreshToken)).willReturn(false);

		assertThatThrownBy(() -> authService.refreshAccessToken(refreshToken))
			.isInstanceOf(CustomException.class)
			.hasMessageContaining(ErrorCode.INVALID_TOKEN.getMessage());
	}

	@Test
	@DisplayName("리프레시 토큰 재발급 실패 - 사용자 없음")
	void refreshAccessToken_userNotFound() {
		String refreshToken = "refresh-token";
		String unknownUserId = UUID.randomUUID().toString();

		given(jwtTokenProvider.validateToken(refreshToken)).willReturn(true);
		given(jwtTokenProvider.getSubject(refreshToken)).willReturn(unknownUserId);
		given(userRepository.findById(UUID.fromString(unknownUserId))).willReturn(Optional.empty());

		assertThatThrownBy(() -> authService.refreshAccessToken(refreshToken))
			.isInstanceOf(CustomException.class)
			.hasMessageContaining(ErrorCode.USER_NOT_FOUND.getMessage());
	}
}

