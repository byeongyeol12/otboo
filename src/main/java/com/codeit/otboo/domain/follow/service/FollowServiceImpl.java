package com.codeit.otboo.domain.follow.service;

import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import com.codeit.otboo.domain.follow.dto.FollowCreateRequest;
import com.codeit.otboo.domain.follow.dto.FollowDto;
import com.codeit.otboo.domain.follow.dto.UserSummary;
import com.codeit.otboo.domain.follow.entity.Follow;
import com.codeit.otboo.domain.follow.repository.FollowRepository;
import com.codeit.otboo.domain.user.entity.User;
import com.codeit.otboo.domain.user.repository.UserRepository;
import com.codeit.otboo.exception.CustomException;
import com.codeit.otboo.global.error.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FollowServiceImpl implements FollowService {

	private final FollowRepository followRepository;
	private final UserRepository userRepository;
	private final ApplicationEventPublisher eventPublisher;

	@Override
	public FollowDto createFollow(FollowCreateRequest request) {
		UUID followerId = request.followerId();
		UUID followeeId = request.followeeId();

		// 팔로워 조회
		User follower = userRepository.findById(followerId).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND,"팔로워를 찾을 수 없습니다."));
		// 대상자 조회
		User followee = userRepository.findById(followeeId).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND,"팔로우할 사람을 찾을 수 없습니다."));

		// 자기 자신 팔로우 방지
		if(follower.equals(followee)) {
			throw new CustomException(ErrorCode.FOLLOW_NOT_MYSELF);
		}
		// 팔로우 중복 방지
		if(followRepository.existsByFollowerAndFollowing(follower,followee)) {
			throw new CustomException(ErrorCode.FOLLOW_ALREADY_USER);
		}

		// 저장
		Follow follow = Follow.builder()
			.id(UUID.randomUUID())
			.follower(follower)
			.following(followee)
			.build();

		followRepository.save(follow);

		//알림 이벤트 발생
		eventPublisher.publishFollowCreatedEvent(follow);

		return new FollowDto(
			follow.getId(),
			new UserSummary(followerId,follower.getName(),follower.getProfileImgUrl());
			new UserSummary(followeeId,followee.getName(),followee.getProfileImgUrl());
		);
	}
}
