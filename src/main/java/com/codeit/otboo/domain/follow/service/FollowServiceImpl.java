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
		User follower = userRepository.findById(followerId).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND,"팔로워를 찾을 수 없습니다."));
		// 대상자 조회
		User following = userRepository.findById(followingId).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND,"팔로우할 사람을 찾을 수 없습니다."));

		// 자기 자신 팔로우 방지
		if(follower.equals(following)) {
			throw new CustomException(ErrorCode.FOLLOW_NOT_MYSELF);
		}
		// 팔로우 중복 방지
		if(followRepository.existsByFollowerIdAndFollowingId(followerId,followingId)) {
			throw new CustomException(ErrorCode.FOLLOW_ALREADY_USER);
		}

		//2. 저장
		Follow follow = Follow.builder()
			.followerId(followerId)
			.followingId(followingId)
			.build();

		followRepository.save(follow);

		//3. 알림 이벤트 발생
		eventPublisher.publishFollowCreatedEvent(follow);

		//4. 리턴
		return new FollowDto(
			follow.getId(),
			follow.getFollowerId(),
			follow.getFollowingId()
		);
	}

	// 내가 팔로우 하는 사람들 목록 조회(상대방 입장 : 내가 팔로워)
	@Override
	public List<FollowDto> getFollowings(UUID followerId) {
		return followRepository.findAllByFollowerId(followerId).stream()
			.map(f-> new FollowDto(f.getId(),f.getFollowingId(),f.getFollowerId()))
			.toList();
	}

	// 나를 팔로우 하는 사람들 목록 조회
	@Override
	public List<FollowDto> getFollowers(UUID followingId) {
		return followRepository.findAllByFollowingId(followingId).stream()
			.map(f-> new FollowDto(f.getId(),f.getFollowingId(),f.getFollowerId()))
			.toList();
	}

	// // 내가 팔로우 하는 사람들 목록 조회
	// @Override
	// public List<FollowDto> getFollowings(UUID followerId) {
	// 	//1. 내가 팔로우 하는 사람들 리스트
	// 	List<Follow> followings = followRepository.findAllByFollowingId(followerId);
	//
	// 	//2. DTO 변환
	// 	List<FollowDto> followeeDtos = new ArrayList<>();
	// 	for(Follow follow : followings) {
	// 		User follower = userRepository.findById(follow.getFollower().getId()).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND,"팔로워를 찾을 수 없습니다."));
	// 		User followee = userRepository.findById(follow.getFollowing().getId()).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND,"팔로우할 사람을 찾을 수 없습니다."));
	//
	// 		FollowDto followDto = new FollowDto(
	// 			follow.getId(),
	// 			new UserSummary(follower.getId(),follower.getName(),follower.getProfileImageUrl()),
	// 			new UserSummary(followee.getId(),followee.getName(),followee.getProfileImageUrl())
	// 		);
	// 		followeeDtos.add(followDto);
	// 	}
	//
	// 	//3. 리스트 반환
	// 	return followeeDtos;
	// }
	//
	// @Override
	// public List<FollowDto> getFollowers(UUID followerId) {
	// 	// 1. 나를 팔로우하는 사람들 리스트
	// 	List<Follow> followers = followRepository.findAllByFollowingId(followeeId);
	//
	// 	// 2. DTO 변환
	// 	List<FollowDto> followerDtos = new ArrayList<>();
	// 	for(Follow follow : followers) {
	// 		User follower = userRepository.findById(follow.getFollower().getId()).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND,"팔로워를 찾을 수 없습니다."));
	// 		User followee = userRepository.findById(follow.getFollowing().getId()).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND,"팔로우할 사람을 찾을 수 없습니다."));
	//
	// 		FollowDto followDto = new FollowDto(
	// 			follow.getId(),
	// 			new UserSummary(follower.getId(),follower.getName(),follower.getProfileImageUrl()),
	// 			new UserSummary(followee.getId(),followee.getName(),followee.getProfileImageUrl())
	// 		);
	// 		followerDtos.add(followDto);
	// 	}
	//
	// 	//3. 리스트 반환
	// 	return followerDtos;
	// }
}
