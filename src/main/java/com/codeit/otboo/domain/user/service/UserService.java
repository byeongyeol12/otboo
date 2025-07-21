package com.codeit.otboo.domain.user.service;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.codeit.otboo.domain.auth.service.TokenCacheService;
import com.codeit.otboo.domain.notification.dto.NotificationDto;
import com.codeit.otboo.domain.notification.entity.NotificationLevel;
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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class UserService {

	private final UserRepository userRepository;
	private final ProfileRepository profileRepository;
	private final UserMapper userMapper;
	private final ProfileMapper profileMapper;
	private final PasswordEncoder passwordEncoder;
	private final TokenCacheService tokenCacheService;
	private final JwtTokenProvider jwtTokenProvider;
	private final ImageStorageService imageStorageService;
	private final NotificationService notificationService;

	@Transactional
	public UserDto create(UserCreateRequest request) {

		if (userRepository.existsByEmail(request.email())) {
			throw new CustomException(ErrorCode.EMAIL_DUPLICATED);
		}

		User user = userMapper.toEntity(request);
		user.setRole(Role.USER);
		user.setLocked(false);
		user.setPasswordHash(passwordEncoder.encode(request.password()));
		userRepository.save(user);

		Profile profile = new Profile(
			user,
			request.name(),
			Gender.MALE
		);
		profileRepository.save(profile);

		return userMapper.toDto(user);
	}

	@Transactional(readOnly = true)
	public UserDtoCursorResponse searchUsers(UserSearchRequest request) {

		// 1. 조건 기반 검색 쿼리 실행 (QueryDSL 또는 동적 JPA)
		List<User> users = userRepository.search(request);

		// 2. 데이터 변환
		List<UserDto> dtos = users.stream()
			.map(UserDto::from)
			.toList();

		// 3. 커서 정보 계산
		String nextCursor = calculateNextCursor(users, request);
		UUID nextIdAfter = users.isEmpty() ? null : users.get(users.size() - 1).getId();
		boolean hasNext = users.size() == request.limit();

		// 4. 총 개수 계산 (옵션)
		long totalCount = userRepository.count(request);

		return new UserDtoCursorResponse(
			dtos,
			nextCursor,
			nextIdAfter,
			hasNext,
			totalCount,
			request.sortBy(),
			request.sortDirection()
		);
	}

	private String calculateNextCursor(List<User> users, UserSearchRequest request) {
		if (users == null || users.isEmpty()) {
			return null;
		}
		UUID lastId = users.get(users.size() - 1).getId();
		return lastId.toString();
	}

	@Transactional
	public UserDto updateUserRole(UUID userId, UserRoleUpdateRequest request) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
		user.updateRole(request.role());

		// 권한 변경 시 알림 발생
		log.info("옷 생성 알림");
		notificationService.createAndSend(
			new NotificationDto(
				UUID.randomUUID(),
				Instant.now(),
				userId,
				"User_Role",
				"권한 ["+request.role()+"] 로 변경되었습니다.",
				NotificationLevel.INFO
			)
		);
		tokenCacheService.invalidateRefreshToken(user.getId());
		return userMapper.toDto(user);
	}

	@Transactional
	public User updateUserLock(UUID userId, boolean locked, String accessToken) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

		user.setLocked(locked);
		jwtTokenProvider.invalidateUserTokens(accessToken);

		return user;
	}

	public ProfileDto getProfile(UUID userId) {
		Profile profile = profileRepository.findByUserId(userId)
			.orElseThrow(() -> new CustomException(ErrorCode.PROFILE_NOT_FOUND));
		return profileMapper.toDto(profile);
	}

	@Transactional
	public ProfileDto updateProfile(UUID userId, ProfileUpdateRequest request, MultipartFile profileImage) {
		Profile profile = profileRepository.findByUserId(userId)
				.orElseThrow(() -> new CustomException(ErrorCode.PROFILE_NOT_FOUND));

		String imageUrl = profile.getProfileImageUrl();
		if (profileImage != null && !profileImage.isEmpty()) {
			imageUrl = imageStorageService.upload(profileImage);
		}

		String nickname = request.nickname() != null ? request.nickname() : profile.getNickname();
		Gender gender = request.gender() != null ? request.gender() : profile.getGender();
		Integer temperatureSensitivity = request.temperatureSensitivity() != null
				? request.temperatureSensitivity()
				: profile.getTemperatureSensitivity();
		Instant birthDateInstant = request.birthDate() != null
				? request.birthDate().atStartOfDay(ZoneOffset.UTC).toInstant()
				: profile.getBirthDate();

		// Location 정보가 있으면 updateLocation 호출
		if (request.location() != null) {
			Double latitude = request.location().latitude();
			Double longitude = request.location().longitude();
			Integer x = request.location().x();
			Integer y = request.location().y();
			String locationNames = String.join(", ", request.location().locationNames());

			profile.updateLocation(latitude, longitude, x, y, locationNames);
		}

		// locationNames는 updateLocation에서 이미 갱신됨, 중복 방지
		profile.updateProfile(
				nickname,
				gender,
				birthDateInstant,
				profile.getLocationNames(), // 현재 profile 객체의 값 사용
				temperatureSensitivity,
				imageUrl
		);

		return profileMapper.toDto(profile);
	}

	@Transactional(readOnly = false)
	public void changePassword(UUID userId, PasswordRequest request, UserPrincipal principal) {
		if (!userId.equals(principal.getId())) {
			throw new CustomException(ErrorCode.FORBIDDEN); // 또는 ErrorCode.NOT_SELF_REQUEST
		}

		User user = userRepository.findById(userId)
			.orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

		if (passwordEncoder.matches(request.newPassword(), user.getPasswordHash())) {
			throw new CustomException(ErrorCode.SAME_AS_OLD_PASSWORD);
		}

		String encodedPassword = passwordEncoder.encode(request.newPassword());
		user.changePassword(encodedPassword);
	}
}
