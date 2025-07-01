package com.codeit.otboo.domain.user.service;

import java.util.List;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.codeit.otboo.domain.auth.service.TokenCacheService;
import com.codeit.otboo.domain.user.dto.request.UserCreateRequest;
import com.codeit.otboo.domain.user.dto.request.UserRoleUpdateRequest;
import com.codeit.otboo.domain.user.dto.request.UserSearchRequest;
import com.codeit.otboo.domain.user.dto.response.UserDto;
import com.codeit.otboo.domain.user.dto.response.UserDtoCursorResponse;
import com.codeit.otboo.domain.user.dto.response.UserSummaryDto;
import com.codeit.otboo.domain.user.entity.User;
import com.codeit.otboo.domain.user.mapper.ProfileMapper;
import com.codeit.otboo.domain.user.mapper.UserMapper;
import com.codeit.otboo.domain.user.repository.UserRepository;
import com.codeit.otboo.exception.CustomException;
import com.codeit.otboo.global.enumType.Role;
import com.codeit.otboo.global.error.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

	private final UserRepository userRepository;
	private final UserMapper userMapper;
	private final ProfileMapper profileMapper;
	private final PasswordEncoder passwordEncoder;
	private final TokenCacheService tokenCacheService;

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

		return userMapper.toDto(user);
	}

	@Transactional(readOnly = true)
	public UserDtoCursorResponse searchUsers(UserSearchRequest request) {

		// 1. 조건 기반 검색 쿼리 실행 (QueryDSL 또는 동적 JPA)
		List<User> users = userRepository.search(request);

		// 2. 데이터 변환
		List<UserSummaryDto> dtos = users.stream()
			.map(UserSummaryDto::from)
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

		tokenCacheService.invalidateRefreshToken(user.getId());
		return userMapper.toDto(user);
	}

}
