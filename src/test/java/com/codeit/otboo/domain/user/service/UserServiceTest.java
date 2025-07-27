package com.codeit.otboo.domain.user.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import com.codeit.otboo.domain.auth.service.TokenCacheService;
import com.codeit.otboo.domain.location.dto.response.LocationResponse;
import com.codeit.otboo.domain.notification.dto.NotificationDto;
import com.codeit.otboo.domain.notification.service.NotificationService;
import com.codeit.otboo.domain.user.dto.request.PasswordRequest;
import com.codeit.otboo.domain.user.dto.request.ProfileUpdateRequest;
import com.codeit.otboo.domain.user.dto.request.UserCreateRequest;
import com.codeit.otboo.domain.user.dto.request.UserRoleUpdateRequest;
import com.codeit.otboo.domain.user.dto.request.UserSearchRequest;
import com.codeit.otboo.domain.user.dto.response.ProfileDto;
import com.codeit.otboo.domain.user.dto.response.UserDto;
import com.codeit.otboo.domain.user.dto.response.UserDtoCursorResponse;
import com.codeit.otboo.domain.user.entity.Profile;
import com.codeit.otboo.domain.user.entity.User;
import com.codeit.otboo.domain.user.mapper.ProfileMapper;
import com.codeit.otboo.domain.user.mapper.UserMapper;
import com.codeit.otboo.domain.user.repository.ProfileRepository;
import com.codeit.otboo.domain.user.repository.UserRepository;
import com.codeit.otboo.exception.CustomException;
import com.codeit.otboo.global.config.jwt.JwtTokenProvider;
import com.codeit.otboo.global.config.security.UserPrincipal;
import com.codeit.otboo.global.enumType.Gender;
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
	@Spy
	private UserMapper userMapper = Mappers.getMapper(UserMapper.class);
	@Mock
	private TokenCacheService tokenCacheService;
	@Mock
	private NotificationService notificationService;
	@Mock
	private UserImageStorageService imageStorageService;

	@Mock
	private JwtTokenProvider jwtTokenProvider;

	@Mock
	private ProfileMapper profileMapper;

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

	@Test
	@DisplayName("프로필 수정 성공 - 위치와 이미지 포함")
	void updateProfile_success() throws Exception {
		// given
		UUID userId = user.getId();
		MultipartFile imageFile = mock(MultipartFile.class);
		given(imageFile.isEmpty()).willReturn(false);
		given(imageStorageService.upload(imageFile, "profiles")).willReturn("uploaded-url");

		LocationResponse location = new LocationResponse(
			37.5, 127.0, 60, 127,
			List.of("서울특별시", "강남구")
		);

		ProfileUpdateRequest request = new ProfileUpdateRequest(
			"newNick", Gender.FEMALE, LocalDate.of(2000, 1, 1),
			location, 4
		);

		Profile profile = new Profile(user, "oldNick", Gender.MALE);
		given(profileRepository.findByUserId(userId)).willReturn(Optional.of(profile));

		// when
		userService.updateProfile(userId, request, imageFile);

		// then
		assertThat(profile.getNickname()).isEqualTo("newNick");
		assertThat(profile.getGender()).isEqualTo(Gender.FEMALE);
		assertThat(profile.getProfileImageUrl()).isEqualTo("uploaded-url");
		assertThat(profile.getLocationNames()).contains("서울특별시");
	}

	@Test
	@DisplayName("프로필 수정 성공 - 이미지 없이")
	void updateProfile_withoutImage_success() throws Exception {
		UUID userId = user.getId();
		MultipartFile imageFile = mock(MultipartFile.class);
		given(imageFile.isEmpty()).willReturn(true);

		Profile profile = new Profile(user, "닉네임", Gender.MALE);
		LocationResponse location = new LocationResponse(
			37.5, 127.0, 60, 127,
			List.of("서울특별시", "강남구")
		);

		ProfileUpdateRequest request = new ProfileUpdateRequest(
			"updatedNick", Gender.FEMALE, LocalDate.of(2000, 2, 2),
			location, 3
		);

		given(profileRepository.findByUserId(userId)).willReturn(Optional.of(profile));
		given(profileMapper.toDto(any(Profile.class))).willReturn(mock(ProfileDto.class));

		userService.updateProfile(userId, request, imageFile);

		assertThat(profile.getNickname()).isEqualTo("updatedNick");
		assertThat(profile.getGender()).isEqualTo(Gender.FEMALE);
		assertThat(profile.getProfileImageUrl()).isNull(); // ✅ 이미지 없으므로 null
	}

	@Test
	@DisplayName("유저 검색 - 기본 요청 성공")
	void searchUsers_basic_success() {
		// given
		User user1 = new User();
		user1.setId(UUID.randomUUID());
		user1.setEmail("user1@email.com");

		User user2 = new User();
		user2.setId(UUID.randomUUID());
		user2.setEmail("user2@email.com");

		List<User> users = List.of(user1, user2);

		UserDto dto1 = UserDto.from(user1);
		UserDto dto2 = UserDto.from(user2);
		List<UserDto> dtoList = List.of(dto1, dto2);

		UserSearchRequest request = new UserSearchRequest();
		request.setLimit(2);
		request.setSortBy("createdAt");
		request.setSortDirection("DESC");
		request.setCursor(null);
		request.setIdAfter(null);
		request.setEmailLike(null);
		request.setRoleEqual(null);
		request.setLocked(null);

		given(userRepository.search(request)).willReturn(users);
		given(userRepository.count(request)).willReturn(2L);

		// when
		UserDtoCursorResponse result = userService.searchUsers(request);

		// then
		assertThat(result.data()).hasSize(2);
		assertThat(result.totalCount()).isEqualTo(2L);
		assertThat(result.hasNext()).isTrue(); // limit과 리스트 길이가 같을 때 true
		assertThat(result.nextIdAfter()).isEqualTo(user2.getId());
		assertThat(result.sortBy()).isEqualTo("createdAt");
		assertThat(result.sortDirection()).isEqualTo("DESC");
	}

	@Test
	@DisplayName("프로필 조회 성공")
	void getProfile_success() {
		UUID userId = user.getId();
		Profile profile = new Profile(user, "nickname", Gender.MALE);

		given(profileRepository.findByUserId(userId)).willReturn(Optional.of(profile));
		given(profileMapper.toDto(profile)).willReturn(mock(ProfileDto.class));

		userService.getProfile(userId);

		verify(profileRepository).findByUserId(userId);
	}

	@Test
	@DisplayName("프로필 조회 실패 - 존재하지 않음")
	void getProfile_notFound() {
		UUID userId = UUID.randomUUID();
		given(profileRepository.findByUserId(userId)).willReturn(Optional.empty());

		assertThatThrownBy(() -> userService.getProfile(userId))
			.isInstanceOf(CustomException.class)
			.hasMessageContaining(ErrorCode.PROFILE_NOT_FOUND.getMessage());
	}

	@Test
	@DisplayName("유저 권한 수정 - 성공")
	void updateUserRole_success() {
		// given
		UUID userId = user.getId();
		UserRoleUpdateRequest request = new UserRoleUpdateRequest(Role.ADMIN);

		given(userRepository.findById(userId)).willReturn(Optional.of(user));

		// when
		UserDto result = userService.updateUserRole(userId, request);

		// then
		assertThat(result.role()).isEqualTo(Role.ADMIN);
		verify(notificationService).createAndSend(any(NotificationDto.class));
		verify(tokenCacheService).invalidateRefreshToken(userId);
	}

	@Test
	@DisplayName("계정 잠금 성공")
	void updateUserLock_success() {
		UUID userId = user.getId();
		String token = "fake-token";

		given(userRepository.findById(userId)).willReturn(Optional.of(user));

		User result = userService.updateUserLock(userId, true, token);

		assertThat(result.isLocked()).isTrue();
		verify(jwtTokenProvider).invalidateUserTokens(token);
	}

	@Test
	@DisplayName("계정 잠금 해제 성공")
	void updateUserUnlock_success() {
		UUID userId = user.getId();
		user.setLocked(true); // 잠금 상태에서 시작

		String token = "fake-token";
		given(userRepository.findById(userId)).willReturn(Optional.of(user));

		User result = userService.updateUserLock(userId, false, token);

		assertThat(result.isLocked()).isFalse();
		verify(jwtTokenProvider).invalidateUserTokens(token);
	}

	@Test
	@DisplayName("비밀번호 변경 성공")
	void changePassword_success() {
		UUID userId = user.getId();
		PasswordRequest request = new PasswordRequest("newPassword123");
		UserPrincipal principal = mock(UserPrincipal.class);

		given(principal.getId()).willReturn(userId);
		given(userRepository.findById(userId)).willReturn(Optional.of(user));
		given(passwordEncoder.matches(any(), any())).willReturn(false);
		given(passwordEncoder.encode("newPassword123")).willReturn("encodedNew");

		userService.changePassword(userId, request, principal);

		assertThat(user.getPasswordHash()).isEqualTo("encodedNew");
	}

	@Test
	@DisplayName("본인이 아닌 경우 비밀번호 변경 실패")
	void changePassword_notSelf() {
		UUID userId = UUID.randomUUID();
		PasswordRequest request = new PasswordRequest("pw");
		UserPrincipal principal = mock(UserPrincipal.class);

		given(principal.getId()).willReturn(UUID.randomUUID());

		assertThatThrownBy(() -> userService.changePassword(userId, request, principal))
			.isInstanceOf(CustomException.class)
			.hasMessageContaining(ErrorCode.FORBIDDEN.getMessage());
	}

	@Test
	@DisplayName("비밀번호 변경 실패 - 이전과 동일한 비밀번호")
	void changePassword_samePassword_throwsException() {
		UUID userId = user.getId();
		PasswordRequest request = new PasswordRequest("newPassword123");

		UserPrincipal principal = new UserPrincipal(
			user.getId(),
			user.getEmail(),
			user.getPasswordHash(),
			user.getRole()
		);
		given(userRepository.findById(userId)).willReturn(Optional.of(user));
		given(passwordEncoder.matches(anyString(), anyString())).willReturn(true);

		assertThatThrownBy(() -> userService.changePassword(userId, request, principal))
			.isInstanceOf(CustomException.class)
			.hasMessageContaining("새 비밀번호는 이전 비밀번호와 달라야 합니다.");
	}
}