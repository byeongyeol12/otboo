package com.codeit.otboo.domain.follow.service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.codeit.otboo.domain.follow.dto.FollowDto;
import com.codeit.otboo.domain.follow.dto.FollowListResponse;
import com.codeit.otboo.domain.follow.dto.FollowSummaryDto;
import com.codeit.otboo.domain.follow.entity.Follow;
import com.codeit.otboo.domain.follow.mapper.FollowMapper;
import com.codeit.otboo.domain.follow.repository.FollowRepository;
import com.codeit.otboo.domain.notification.dto.NotificationDto;
import com.codeit.otboo.domain.notification.entity.NotificationLevel;
import com.codeit.otboo.domain.notification.service.NotificationService;
import com.codeit.otboo.domain.user.entity.User;
import com.codeit.otboo.domain.user.repository.UserRepository;
import com.codeit.otboo.domain.user.service.UserService;
import com.codeit.otboo.exception.CustomException;
import com.codeit.otboo.global.error.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class FollowServiceImpl implements FollowService {

	private final FollowRepository followRepository;
	private final UserRepository userRepository;
	private final UserService userService;
	private final FollowMapper followMapper;
	private final NotificationService notificationService;

	//팔로우 생성
	@Override
	public FollowDto createFollow(UUID myUserId,UUID followeeId) {
		//1. 팔로우, 팔로워 조회 및 예외 처리
		// 팔로워(=나) 조회
		User follower = userRepository.findById(myUserId)
			.orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND, "팔로워를 찾을 수 없습니다."));
		// 대상자 조회
		User followee = userRepository.findById(followeeId)
			.orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND, "팔로우할 사람을 찾을 수 없습니다."));

		// 자기 자신 팔로우 방지
		if (follower.getId().equals(followee.getId())) {
			throw new CustomException(ErrorCode.FOLLOW_NOT_MYSELF);
		}
		// 팔로우 중복 방지
		if (followRepository.existsByFollowerIdAndFolloweeId(follower.getId(), followee.getId())) {
			throw new CustomException(ErrorCode.FOLLOW_ALREADY_USER);
		}

		//2. 저장
		Follow follow = Follow.builder()
			.follower(follower)
			.followee(followee)
			.build();

		followRepository.save(follow);

		//3. 알림 이벤트 발생
		log.info("팔로우 생성 알림");
		notificationService.createAndSend(
			new NotificationDto(
				UUID.randomUUID(),
				Instant.now(),
				followeeId,
				"Follow",
				"새 팔로워 ["+follower.getName()+"] 님이 [" +followee.getName()+ "] 님을 팔로우 했습니다.",
				NotificationLevel.INFO
			)
		);

		//4. 리턴
		return followMapper.toFollowDto(follow);
	}

	// 팔로우 요약 정보 조회
	@Override
	public FollowSummaryDto getFollowSummary(UUID followeeId, UUID myUserId) {
		//1. 유저 정보 조회
		User user = userRepository.getUserById(followeeId); // 요약 정보를 조회할 대상
		User me = userRepository.getUserById(myUserId);

		//2. 팔로워 수 조회
		long followerCount = followRepository.countByFolloweeId(user.getId()); // 대상을 팔로우하는 수
		long followeeCount = followRepository.countByFollowerId(user.getId()); // 대상이 팔로우하고 있는 수

		//3. 팔로우 중인지 확인
		Optional<Follow> followedByMe = followRepository.findByFollowerAndFollowee(me,user); // 내가 이 유저를 팔로우하고 있는지
		boolean followeeMe = followRepository.existsByFollowerAndFollowee(user,me); // 상대가 나를 팔로우하고 있는지

		//4. return
		return followMapper.toFollowSummaryDto(
			followeeId,
			followerCount,
			followeeCount,
			followedByMe.isPresent(),
			followedByMe.map(Follow::getId).orElse(null),
			followeeMe
		);
	}


	// 내가 팔로우 하는 사람들 목록 조회(상대방 입장 : 내가 팔로워)
	@Override
	public FollowListResponse getFollowings(UUID followerId,String cursor,UUID idAfter,int limit,String nameLike,String sortBy,String sortDirection) {
		// 1. 커서 변환(cursor 값이 있으면 우선 적용 없으면 idAfter 사용)
		UUID effectiveIdAfter = (cursor != null && !cursor.isBlank())
			? UUID.fromString(cursor)
			: idAfter;

		//2. 정렬
		Sort.Direction direction = "DESCENDING".equalsIgnoreCase(sortDirection) ? Sort.Direction.DESC : Sort.Direction.ASC;
		String sort = (sortBy != null && !sortBy.isBlank()) ? sortBy : "id";
		Pageable pageable = PageRequest.of(0, limit+1, Sort.by(direction, sort));

		//3. repository query
		List<Follow> follows = followRepository.findFollowees(followerId,effectiveIdAfter,nameLike,pageable);

		//4. hasNext, nextCursor
		boolean hasNext = follows.size() > limit;
		List<Follow> pagedList = hasNext ? follows.subList(0, limit) : follows;

		String nextCursor = hasNext ? pagedList.get(pagedList.size() - 1).getId().toString() : null;
		UUID nextIdAfter = hasNext ? pagedList.get(pagedList.size() - 1).getId() : null;

		// 5. 전체 카운트
		long totalCount = followRepository.countByFollowerId(followerId);

		//6. dto 변환
		List<FollowDto> dtoList = followMapper.toFollowDtoList(follows);

		//7. return
		return new FollowListResponse(
			dtoList,
			nextCursor,
			nextIdAfter,
			hasNext,
			totalCount,
			sortBy,
			sortDirection
		);
	}

	// 나를 팔로우 하는 사람들 목록 조회
	@Override
	public FollowListResponse getFollowers(UUID followeeId,String cursor,UUID idAfter,int limit,String nameLike,String sortBy,String sortDirection) {
		// 1. 커서 변환(cursor 값이 있으면 우선 적용 없으면 idAfter 사용)
		UUID effectiveIdAfter = (cursor != null && !cursor.isBlank())
			? UUID.fromString(cursor)
			: idAfter;

		//2. 정렬
		Sort.Direction direction = "DESCENDING".equalsIgnoreCase(sortDirection) ? Sort.Direction.DESC : Sort.Direction.ASC;
		String sort = (sortBy != null && !sortBy.isBlank()) ? sortBy : "id";
		Pageable pageable = PageRequest.of(0, limit+1, Sort.by(direction, sort));

		//3. repository query
		List<Follow> follows = followRepository.findFollowers(followeeId,effectiveIdAfter,nameLike,pageable);

		//4. hasNext, nextCursor
		boolean hasNext = follows.size() > limit;
		List<Follow> pagedList = hasNext ? follows.subList(0, limit) : follows;

		String nextCursor = hasNext ? pagedList.get(pagedList.size() - 1).getId().toString() : null;
		UUID nextIdAfter = hasNext ? pagedList.get(pagedList.size() - 1).getId() : null;

		// 5. 전체 카운트
		long totalCount = followRepository.countByFolloweeId(followeeId);

		//6. dto 변환
		List<FollowDto> dtoList = followMapper.toFollowDtoList(pagedList);

		//7. return
		return new FollowListResponse(
			dtoList,
			nextCursor,
			nextIdAfter,
			hasNext,
			totalCount,
			sortBy,
			sortDirection
		);
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
