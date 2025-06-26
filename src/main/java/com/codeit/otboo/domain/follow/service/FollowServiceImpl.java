package com.codeit.otboo.domain.follow.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import com.codeit.otboo.domain.follow.dto.FollowCreateRequest;
import com.codeit.otboo.domain.follow.dto.FollowDto;
import com.codeit.otboo.domain.follow.dto.UserSummary;
import com.codeit.otboo.domain.follow.entity.Follow;
import com.codeit.otboo.domain.follow.repository.FollowRepository;
import com.codeit.otboo.domain.follow.entity.User;
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

	//팔로우 생성
	@Override
	public FollowDto createFollow(FollowCreateRequest request) {
		//1. 팔로우, 팔로워 조회 및 예외 처리
		UUID followerId = request.followerId();
		UUID followingId = request.followeeId();

		// 팔로워 조회
		User follower = userRepository.findById(followerId)
			.orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND, "팔로워를 찾을 수 없습니다."));
		// 대상자 조회
		User following = userRepository.findById(followingId)
			.orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND, "팔로우할 사람을 찾을 수 없습니다."));

		// 자기 자신 팔로우 방지
		if (follower.equals(following)) {
			throw new CustomException(ErrorCode.FOLLOW_NOT_MYSELF);
		}
		// 팔로우 중복 방지
		if (followRepository.existsByFollowerIdAndFollowingId(followerId, followingId)) {
			throw new CustomException(ErrorCode.FOLLOW_ALREADY_USER);
		}

		//2. 저장
		Follow follow = Follow.builder()
			.follower(follower)
			.following(following)
			.build();

		followRepository.save(follow);

		//3. 알림 이벤트 발생
		eventPublisher.publishFollowCreatedEvent(follow);

		//4. 리턴
		return new FollowDto(
			follow.getId(),
			new UserSummary(follower.getId(), follower.getName(), follower.getProfileImageUrl()),
			new UserSummary(following.getId(), following.getName(), following.getProfileImageUrl())
		);
	}

	// 내가 팔로우 하는 사람들 목록 조회(상대방 입장 : 내가 팔로워)
	@Override
	public List<FollowDto> getFollowings(UUID followerId) {
		// 1. 내가 팔로우 하는 사람들 목록 전체 조회
		return followRepository.findAllByFollowerId(followerId).stream()
			.map(f -> //DTO 변환
				new FollowDto(
					f.getId(),
					new UserSummary(f.getFollower().getId(), f.getFollower().getName(),
						f.getFollower().getProfileImageUrl()),
					new UserSummary(f.getFollowing().getId(), f.getFollowing().getName(),
						f.getFollowing().getProfileImageUrl()))
			)
			.toList();
	}

	// 나를 팔로우 하는 사람들 목록 조회
	@Override
	public List<FollowDto> getFollowers(UUID followingId) {
		// 1. 나를 팔로우 하는 사람들 목록 전체 조회
		return followRepository.findAllByFollowingId(followingId).stream()
			.map(f -> // 2. DTO 변환
				new FollowDto(
					f.getId(),
					new UserSummary(f.getFollower().getId(), f.getFollower().getName(),
						f.getFollower().getProfileImageUrl()),
					new UserSummary(f.getFollowing().getId(), f.getFollowing().getName(),
						f.getFollowing().getProfileImageUrl()))
			)
			.toList();
	}

	// 팔로우 취소(매개변수 : 취소 하려는 팔로우의 PK , 현재 로그인한 유저의 ID)
	public void cancelFollow(UUID followId, UUID loginUserId) {
		// 1. 팔로우 존재 여부 확인
		Follow follow = followRepository.findById(followId)
			.orElseThrow(() -> new CustomException(ErrorCode.FOLLOW_NOT_FOUND, "팔로우 관계를 찾을 수 없습니다."));
		// 2. 본인 확인(찾은 팔로우 관계의 팔로우를 건 사람과 현재 로그인 유저 비교)
		if (!follow.getFollower().getId().equals(loginUserId)) {
			throw new CustomException(ErrorCode.FOLLOW_CANCEL_ONLY_MINE, "본인의 팔로우만 취소할 수 있습니다.");
		}
		// 3. 팔로우 삭제
		followRepository.deleteById(followId);
	}

}
