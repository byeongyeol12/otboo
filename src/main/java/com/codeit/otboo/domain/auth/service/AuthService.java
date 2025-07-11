package com.codeit.otboo.domain.auth.service;

import java.time.Instant;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.codeit.otboo.domain.auth.dto.request.LoginRequest;
import com.codeit.otboo.domain.auth.dto.response.LoginResponse;
import com.codeit.otboo.domain.notification.dto.NotificationDto;
import com.codeit.otboo.domain.notification.entity.NotificationLevel;
import com.codeit.otboo.domain.notification.service.NotificationService;
import com.codeit.otboo.domain.user.entity.User;
import com.codeit.otboo.domain.user.repository.UserRepository;
import com.codeit.otboo.exception.CustomException;
import com.codeit.otboo.global.config.jwt.JwtTokenProvider;
import com.codeit.otboo.global.error.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtTokenProvider jwtTokenProvider;
	private final TokenCacheService tokenCacheService;
	private final NotificationService notificationService;

	public LoginResponse login(LoginRequest request) {

		User user = userRepository.findByEmail(request.getEmail())
			.orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

		if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
			throw new CustomException(ErrorCode.PASSWORD_MISMATCH);
		}

		String token = jwtTokenProvider.generateToken(
			user.getId(),
			user.getEmail(),
			user.getName(),
			user.getRole().name()
		);

		String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());

		tokenCacheService.invalidateRefreshToken(user.getId());
		tokenCacheService.saveRefreshToken(user.getId(), refreshToken);

		Instant expiresAt = Instant.now().plusMillis(jwtTokenProvider.getTokenValidityInMilliseconds());

		// 알림 테스트
		notificationService.createAndSend(
			new NotificationDto(
				UUID.randomUUID(),
				Instant.now(),
				user.getId(),
				"로그인",
				"유저 ["+user.getName()+"] 님이 로그인 했습니다.",
				NotificationLevel.INFO
			)
		);

		return new LoginResponse(token, refreshToken, expiresAt);
	}

	public void logout(String token) {
		jwtTokenProvider.validateToken(token);
	}

	public String refreshAccessToken(String refreshToken) {
		if (!jwtTokenProvider.validateToken(refreshToken)) {
			throw new CustomException(ErrorCode.INVALID_TOKEN);
		}

		UUID userId = UUID.fromString(jwtTokenProvider.getSubject(refreshToken));

		User user = userRepository.findById(userId)
			.orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

		return jwtTokenProvider.generateToken(user.getId(), user.getEmail(), user.getName(), user.getRole().name());
	}
}
