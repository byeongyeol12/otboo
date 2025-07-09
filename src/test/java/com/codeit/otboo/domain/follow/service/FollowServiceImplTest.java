package com.codeit.otboo.domain.follow.service;

import static org.assertj.core.api.Assertions.*;
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

import com.codeit.otboo.domain.follow.dto.FollowCreateRequest;
import com.codeit.otboo.domain.follow.dto.FollowDto;
import com.codeit.otboo.domain.follow.dto.UserSummary;
import com.codeit.otboo.domain.follow.entity.Follow;
import com.codeit.otboo.domain.follow.mapper.FollowMapper;
import com.codeit.otboo.domain.follow.repository.FollowRepository;
import com.codeit.otboo.domain.notification.entity.NotificationLevel;
import com.codeit.otboo.domain.notification.service.NotificationService;
import com.codeit.otboo.domain.user.entity.User;
import com.codeit.otboo.domain.user.repository.UserRepository;
import com.codeit.otboo.domain.user.service.UserService;

@ExtendWith(MockitoExtension.class)
public class FollowServiceImplTest {

	@InjectMocks
	private FollowServiceImpl followService;

	@Mock
	private FollowRepository followRepository;
	@Mock
	private UserRepository userRepository;
	@Mock
	private UserService userService;
	@Mock
	private FollowMapper followMapper;
	@Mock
	private NotificationService notificationService;

	private static final UUID followerId = UUID.fromString("00000000-0000-0000-0000-000000000001");
	private static final UUID followeeId = UUID.fromString("00000000-0000-0000-0000-000000000002");
	private User follower;
	private User followee;

	@BeforeEach
	public void setUp() {
		follower = new User();
		follower.setId(followerId);
		follower.setName("팔로워");

		followee = new User();
		followee.setId(followeeId);
		followee.setName("팔로이");
	}

	//createFollow
	@Test
	@DisplayName("createFollow - 팔로우 생성 성공")
	public void createFollow_Success(){
		//given
		//FollowCreateRequest 생성
		FollowCreateRequest request = new FollowCreateRequest(followerId, followeeId);

		//팔로워,팔로이 모두 존재한다고 가정
		given(userRepository.findById(followerId)).willReturn(Optional.of(follower));
		given(userRepository.findById(followeeId)).willReturn(Optional.of(followee));

		//중복 팔로우 방지
		given(followRepository.existsByFollowerIdAndFolloweeId(followeeId, followerId)).willReturn(false);

		//팔로우 저장
		Follow follow = Follow.builder().follower(follower).followee(followee).build();
		given(followRepository.save(any(Follow.class))).willReturn(follow);

		//dto 변환
		FollowDto dto = new FollowDto(
			UUID.randomUUID(),
			new UserSummary(
				followee.getId(),
				followee.getName(),
				null
			),
			new UserSummary(
				follower.getId(),
				follower.getName(),
				null
			));
		given(followMapper.toFollowDto(any(Follow.class))).willReturn(dto);

		//when
		FollowDto result = followService.createFollow(request);

		//then
		//반환된 dto != null
		assertThat(result).isNotNull();
		//알림 서비스 호출 확인
		then(notificationService).should(times(1))
			.createAndSend(eq(followerId),eq("팔로우"),contains("새 팔로워"),eq(NotificationLevel.INFO));
	}
}
