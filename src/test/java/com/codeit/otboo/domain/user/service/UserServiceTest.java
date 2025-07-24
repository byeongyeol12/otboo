package com.codeit.otboo.domain.user.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.codeit.otboo.domain.auth.service.TokenCacheService;
import com.codeit.otboo.domain.notification.service.NotificationService;
import com.codeit.otboo.domain.user.dto.request.UserCreateRequest;
import com.codeit.otboo.domain.user.dto.response.UserDto;
import com.codeit.otboo.domain.user.entity.User;
import com.codeit.otboo.domain.user.mapper.UserMapper;
import com.codeit.otboo.domain.user.repository.ProfileRepository;
import com.codeit.otboo.domain.user.repository.UserRepository;
import com.codeit.otboo.exception.CustomException;
import com.codeit.otboo.global.enumType.Role;
import com.codeit.otboo.global.error.ErrorCode;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

	@InjectMocks
	private UserService userService;

	@Mock
	private UserRepository userRepository;
	@Mock
	private ProfileRepository profileRepository;
	@Mock
	private PasswordEncoder passwordEncoder;
	@Mock
	private UserMapper userMapper;
	@Mock
	private TokenCacheService tokenCacheService;
	@Mock
	private NotificationService notificationService;
	@Mock
	private ImageStorageService imageStorageService;

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
	@DisplayName("회원가입 성공")
	void createUser_success() {
		String email = "test-success@ootd.com"; // 🔧 고유 이메일 사용
		UserCreateRequest request = new UserCreateRequest("테스터", email, rawPassword);
		user.setEmail(email);

		given(userRepository.existsByEmail(email)).willReturn(false);
		given(passwordEncoder.encode(rawPassword)).willReturn(hashedPassword);
		given(userMapper.toEntity(request)).willReturn(user);
		given(userMapper.toDto(user)).willReturn(UserDto.from(user));

		UserDto result = userService.create(request);

		assertThat(result.email()).isEqualTo(email);
		verify(profileRepository).save(any());
	}

	@Test
	@DisplayName("중복 이메일 회원가입 실패")
	void createUser_duplicateEmail() {
		String email = "test-duplicate@ootd.com"; // 🔧 고유 이메일 사용
		UserCreateRequest request = new UserCreateRequest("테스터", email, rawPassword);
		given(userRepository.existsByEmail(email)).willReturn(true);

		assertThatThrownBy(() -> userService.create(request))
			.isInstanceOf(CustomException.class)
			.hasMessageContaining(ErrorCode.EMAIL_DUPLICATED.getMessage());
	}

	@Test
	@DisplayName("회원가입 시 비밀번호 인코딩 확인")
	void createUser_passwordEncoding() {
		String email = "test-password@ootd.com"; // 🔧 고유 이메일 사용
		UserCreateRequest request = new UserCreateRequest("테스터", email, rawPassword);

		given(userRepository.existsByEmail(email)).willReturn(false);
		given(passwordEncoder.encode(rawPassword)).willReturn(hashedPassword);
		given(userMapper.toEntity(request)).willReturn(user);
		given(userMapper.toDto(user)).willReturn(UserDto.from(user));

		userService.create(request);

		verify(passwordEncoder).encode(rawPassword);
	}
}