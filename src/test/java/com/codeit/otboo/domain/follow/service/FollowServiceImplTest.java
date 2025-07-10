package com.codeit.otboo.domain.follow.service;

import static java.util.Collections.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.codeit.otboo.domain.follow.dto.FollowDto;
import com.codeit.otboo.domain.follow.dto.FollowListResponse;
import com.codeit.otboo.domain.follow.dto.FollowSummaryDto;
import com.codeit.otboo.domain.follow.entity.Follow;
import com.codeit.otboo.domain.follow.mapper.FollowMapper;
import com.codeit.otboo.domain.follow.repository.FollowRepository;
import com.codeit.otboo.domain.notification.entity.NotificationLevel;
import com.codeit.otboo.domain.notification.service.NotificationService;
import com.codeit.otboo.domain.user.dto.response.UserSummaryDto;
import com.codeit.otboo.domain.user.entity.User;
import com.codeit.otboo.domain.user.repository.UserRepository;
import com.codeit.otboo.domain.user.service.UserService;
import com.codeit.otboo.exception.CustomException;
import com.codeit.otboo.global.error.ErrorCode;

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
	private static final UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000003");
	private User follower;
	private User followee;
	private User user;

	@BeforeEach
	public void setUp() {
		// 팔로워(유저 , 팔로우를 거는 사람)
		follower = new User();
		follower.setId(followerId);
		follower.setName("팔로워");

		// 팔로이(팔로우 대상)
		followee = new User();
		followee.setId(followeeId);
		followee.setName("팔로이");

		User user = new User();
		user.setId(userId);
		user.setName("유저");
	}

	//createFollow
	@Test
	@DisplayName("createFollow - 팔로우 생성 성공 및 알림 발생")
	public void createFollow_success() {
		//given

		//팔로워,팔로이 모두 존재한다고 가정
		when(userRepository.findById(followerId)).thenReturn(Optional.of(follower));
		when(userRepository.findById(followeeId)).thenReturn(Optional.of(followee));

		//중복 팔로우 체크
		when(followRepository.existsByFollowerIdAndFolloweeId(followerId, followeeId)).thenReturn(false);

		//팔로우 저장
		Follow follow = Follow.builder().follower(follower).followee(followee).build();
		when(followRepository.save(any(Follow.class))).thenReturn(follow);

		//dto 변환
		FollowDto dto = new FollowDto(
			UUID.randomUUID(),
			new UserSummaryDto(followee.getId(), followee.getName(), null),
			new UserSummaryDto(follower.getId(), follower.getName(), null)
		);
		when(followMapper.toFollowDto(any(Follow.class))).thenReturn(dto);

		//when
		FollowDto result = followService.createFollow(followerId, followeeId);

		//then
		assertNotNull(result); // 팔로우 결과가 정상
		verify(followRepository).save(any(Follow.class)); // 팔로우 저장 호출 검증

		// 알림(NotificationService)이 정상적으로 호출되었는지 검증
		verify(notificationService, times(1))
			.createAndSend(argThat(notificationDto ->
				notificationDto.receiverId().equals(followeeId)  // 알림 받는 사람이 팔로워
					&& notificationDto.title().equals("팔로우")       // 알림 제목이 "팔로우"이고
					&& notificationDto.content().contains("[" + follower.getName() + "]") // 내용에 팔로워 이름 포함
					&& notificationDto.level() == NotificationLevel.INFO // 알림 레벨 INFO
			));
	}

	@Test
	@DisplayName("createFollow - 팔로워가 존재하지 않으면 예외 발생")
	public void createFollow_fail_followerNotFound() {
		//given
		when(userRepository.findById(followerId)).thenReturn(Optional.empty());

		//when,then
		CustomException ex = assertThrows(CustomException.class, () -> followService.createFollow(followerId, followeeId));
		assertEquals(ErrorCode.USER_NOT_FOUND, ex.getErrorCode());
		verify(notificationService, never()).createAndSend(any());
	}

	@Test
	@DisplayName("createFollow - 팔로이가 존재하지 않으면 예외 발생")
	public void createFollow_fail_followeeNotFound() {
		//given
		when(userRepository.findById(followerId)).thenReturn(Optional.of(follower));
		when(userRepository.findById(followeeId)).thenReturn(Optional.empty());

		//when,then
		CustomException ex = assertThrows(CustomException.class, () -> followService.createFollow(followerId, followeeId));
		assertEquals(ErrorCode.USER_NOT_FOUND, ex.getErrorCode());
		verify(notificationService, never()).createAndSend(any());
	}

	@Test
	@DisplayName("createFollow - 자기 자신을 팔로우 시도하면 예외 발생")
	public void createFollow_fail_follow_myself() {
		//given

		//팔로워만 존재
		when(userRepository.findById(followerId)).thenReturn(Optional.of(follower));
		when(userRepository.findById(followerId)).thenReturn(Optional.of(follower));

		//when,then
		CustomException ex = assertThrows(CustomException.class, () -> followService.createFollow(followerId, followerId));
		assertEquals(ErrorCode.FOLLOW_NOT_MYSELF, ex.getErrorCode());
		verify(notificationService, never()).createAndSend(any());
	}

	@Test
	@DisplayName("createFollow - 이미 팔로우 관계가 있으면 예외 발생")
	public void createFollow_fail_follow_duplicated() {
		//given

		//팔로워,팔로이 모두 존재한다고 가정
		when(userRepository.findById(followerId)).thenReturn(Optional.of(follower));
		when(userRepository.findById(followeeId)).thenReturn(Optional.of(followee));

		//중복 발생
		when(followRepository.existsByFollowerIdAndFolloweeId(any(), any())).thenReturn(true);

		//when,then
		CustomException ex = assertThrows(CustomException.class, () -> followService.createFollow(followerId,followeeId));
		assertEquals(ErrorCode.FOLLOW_ALREADY_USER,ex.getErrorCode());
		verify(notificationService,never()).createAndSend(any());
	}

	//getFollowSummary - 팔로우 요약 정보 조회
	@Test
	@DisplayName("getFollowSummary - 팔로우 요약 조회(서로 팔로우)")
	public void getFollowSummary_success() {
		//given
		// 팔로이, 팔로워 조회
		when(userRepository.getUserById(followeeId)).thenReturn(followee);
		when(userRepository.getUserById(followerId)).thenReturn(follower);
		// 팔로워 수 조회
		when(followRepository.countByFolloweeId(followeeId)).thenReturn(1L); // 내가 팔로우하는 사람 수(팔로잉 수)
		when(followRepository.countByFollowerId(followeeId)).thenReturn(2L); // 나를 팔로우하는 사람 수(팔로워 수)

		// 팔로우 중인지 확인
		Follow follow  = Follow.builder().follower(follower).followee(followee).build();
		when(followRepository.findByFollowerAndFollowee(follower,followee)).thenReturn(Optional.of(follow));
		when(followRepository.existsByFollowerAndFollowee(followee,follower)).thenReturn(true);

		// Mapper mock 처리
		FollowSummaryDto dto = new FollowSummaryDto(
			followeeId,   // 팔로이 ID
			1L,           // 팔로워 수
			2L,           // 팔로잉 수
			true,         // 내가 이 사람을 팔로우 하는가
			follow.getId(), // follow id
			true          // 이 사람이 나를 팔로우 하는가
		);
		when(followMapper.toFollowSummaryDto(followeeId, 1L, 2L, true, follow.getId(), true)).thenReturn(dto);

		//when
		FollowSummaryDto summary = followService.getFollowSummary(followeeId,followerId);

		// then
		assertNotNull(summary);                               // 요약 결과가 null이 아닌지 확인
		assertEquals(followeeId, summary.followeeId());       // 팔로이 ID 확인
		assertEquals(1L, summary.followerCount());            // 팔로워 수 확인
		assertEquals(2L, summary.followingCount());           // 팔로잉 수 확인
		assertTrue(summary.followedByMe());                   // 내가 이 사람을 팔로우하는지 확인
		assertEquals(follow.getId(), summary.followedByMeId()); // followId 확인
		assertTrue(summary.followingMe());                    // 이 사람이 나를 팔로우하는지 확인
	}

	@Test
	@DisplayName("getFollowSummary - 팔로우 요약 정보(팔로워가 팔로이 를 팔로우하지 않은 경우)")
	void getFollowSummary_follower_notFollowed(){
		//given
		// 팔로이, 팔로워 조회
		when(userRepository.getUserById(followeeId)).thenReturn(followee);
		when(userRepository.getUserById(followerId)).thenReturn(follower);
		// 팔로워 수 조회
		when(followRepository.countByFolloweeId(followeeId)).thenReturn(1L); // 내가 팔로우하는 사람 수(팔로잉 수)
		when(followRepository.countByFollowerId(followeeId)).thenReturn(2L); // 나를 팔로우하는 사람 수(팔로워 수)

		// 팔로우 중인지 확인
		Follow follow  = Follow.builder().follower(follower).followee(followee).build();
		when(followRepository.findByFollowerAndFollowee(follower,followee)).thenReturn(Optional.empty());
		when(followRepository.existsByFollowerAndFollowee(followee,follower)).thenReturn(true);

		// Mapper mock 처리
		FollowSummaryDto dto = new FollowSummaryDto(
			followeeId,   // 팔로이 ID
			1L,           // 팔로워 수
			2L,           // 팔로잉 수
			false,         // 내가 이 사람을 팔로우 하는가
			null, // follow id
			true          // 이 사람이 나를 팔로우 하는가
		);
		when(followMapper.toFollowSummaryDto(followeeId, 1L, 2L, false, null, true)).thenReturn(dto);

		//when
		FollowSummaryDto summary = followService.getFollowSummary(followeeId,followerId);

		// then
		assertNotNull(summary);                               // 요약 결과가 null이 아닌지 확인
		assertEquals(followeeId, summary.followeeId());       // 팔로이 ID 확인
		assertEquals(1L, summary.followerCount());            // 팔로워 수 확인
		assertEquals(2L, summary.followingCount());           // 팔로잉 수 확인
		assertFalse(summary.followedByMe());                  // 내가 이 사람을 팔로우하지 않는지 확인
		assertNull(summary.followedByMeId());                 // followId는 null이어야 함
		assertTrue(summary.followingMe());                    // 이 사람이 나를 팔로우하는지 확인
	}


	// getFollowings - 	유저가 팔로우 하는 사람들 목록 조회(팔로우 클릭)
	@Test
	@DisplayName("getFollowings - 팔로잉 목록 조회 성공(cursor X, idAfter X, limit 2, nameLike X , hasNext true)")
	public void getFollowing_noCursor_noIdAfter_limit2_noNameLike() {
		//given
		int limit = 2;

		// 현재 팔로우 3개(limit 2 + 1)
		List<Follow> followList = List.of(
			Follow.builder().follower(follower).followee(followee).build(),
			Follow.builder().follower(follower).followee(followee).build(),
			Follow.builder().follower(follower).followee(followee).build()
		);

		//dto 변환
		List<FollowDto> dtoList = List.of(
			new FollowDto(
				UUID.randomUUID(),
				new UserSummaryDto(followee.getId(), followee.getName(), null),
				new UserSummaryDto(follower.getId(), follower.getName(), null)
			),
			new FollowDto(
				UUID.randomUUID(),
				new UserSummaryDto(followee.getId(), followee.getName(), null),
				new UserSummaryDto(follower.getId(), follower.getName(), null)
			)
		);

		// 팔로잉 목록 리턴
		when(followRepository.findFollowees(eq(followerId), isNull(), isNull(), any()))
			.thenReturn(followList);

		// 총 팔로잉 수 조회
		when(followRepository.countByFollowerId(followerId)).thenReturn(3L);
		// Mapper Mock 처리
		when(followMapper.toFollowDtoList(any())).thenReturn(dtoList);

		//when
		FollowListResponse result = followService.getFollowings(
			followerId, null, null, limit, null, "id", "ASCENDING"
		);

		//then
		assertNotNull(result); // 결과 객체가 null이 아님을 확인
		assertEquals(2, result.data().size()); // 반환된 데이터가 2개(limit만큼)인지 확인
		assertTrue(result.hasNext()); // 다음 페이지가 있는지(hasNext=true) 확인
		assertNotNull(result.nextCursor()); // 다음 페이지 커서가 정상적으로 리턴되는지 확인
	}

	@Test
	@DisplayName("getFollowings - 팔로잉 목록 조회 성공(cursor O, idAfter X, limit 2, nameLike O , hasNext:false)")
	public void getFollowings_cursorUsed_noIdAfter_limit2_nameLikeUsed() {
		//given
		String cursor = UUID.randomUUID().toString();
		int limit = 2;
		String nameLike = "팔";

		// 현재 팔로우 2개
		List<Follow> followList = List.of(
			Follow.builder().follower(follower).followee(followee).build(),
			Follow.builder().follower(follower).followee(followee).build()
		);

		//dto 변환
		List<FollowDto> dtoList = List.of(
			new FollowDto(
				UUID.randomUUID(),
				new UserSummaryDto(followee.getId(), followee.getName(), null),
				new UserSummaryDto(follower.getId(), follower.getName(), null)
			),
			new FollowDto(
				UUID.randomUUID(),
				new UserSummaryDto(followee.getId(), followee.getName(), null),
				new UserSummaryDto(follower.getId(), follower.getName(), null)
			)
		);

		//mock
		when(followRepository.findFollowees(eq(followerId), eq(UUID.fromString(cursor)), eq(nameLike), any()))
			.thenReturn(followList);
		when(followRepository.countByFollowerId(followerId)).thenReturn(2L);
		when(followMapper.toFollowDtoList(followList)).thenReturn(dtoList);

		//when
		FollowListResponse result = followService.getFollowings(
			followerId, cursor, null, limit, nameLike, "id", "ASCENDING");

		//then
		assertNotNull(result); // 결과 not null
		assertEquals(2, result.data().size()); // 데이터 일치
		assertNull(result.nextCursor()); // 다음 커서 존재x
		assertNull(result.nextIdAfter());
		assertFalse(result.hasNext()); // hasNext
		assertEquals(2L, result.totalCount()); // 총 카운트 확인
		assertEquals("id", result.sortBy());
		assertEquals("ASCENDING", result.sortDirection());
	}

	@Test
	@DisplayName("getFollowings - 팔로잉 목록 조회(cursor X, idAfter O, limit 3, nameLike X, hasNext X)")
	public void getFollowings_noCursor_idAfterUsed_limit3_noNameLike(){
		//given
		UUID idAfter = UUID.randomUUID();
		int limit = 3;

		// 현재 팔로우 2개
		List<Follow> followList = List.of(
			Follow.builder().follower(follower).followee(followee).build(),
			Follow.builder().follower(follower).followee(followee).build()
		);

		//dto 변환
		List<FollowDto> dtoList = List.of(
			new FollowDto(
				UUID.randomUUID(),
				new UserSummaryDto(followee.getId(), followee.getName(), null),
				new UserSummaryDto(follower.getId(), follower.getName(), null)
			),
			new FollowDto(
				UUID.randomUUID(),
				new UserSummaryDto(followee.getId(), followee.getName(), null),
				new UserSummaryDto(follower.getId(), follower.getName(), null)
			)
		);

		//mock
		when(followRepository.findFollowees(eq(followerId), eq(idAfter), isNull(), any()))
			.thenReturn(followList);
		when(followRepository.countByFollowerId(followerId)).thenReturn(2L);
		when(followMapper.toFollowDtoList(followList)).thenReturn(dtoList);

		//when
		FollowListResponse result = followService.getFollowings(
			followerId, null, idAfter, limit, null, "id", "ASCENDING");

		//then
		assertNotNull(result); // 결과 not null
		assertEquals(2, result.data().size()); // 데이터 일치
		assertNull(result.nextCursor()); // 다음 커서 존재x
		assertNull(result.nextIdAfter());
		assertFalse(result.hasNext()); // hasNext
		assertEquals(2L, result.totalCount()); // 총 카운트 확인
		assertEquals("id", result.sortBy());
		assertEquals("ASCENDING", result.sortDirection());
	}

	@Test
	@DisplayName("getFollowings - 팔로잉 목록 조회(cursor O, idAfter X, limit 5, nameLike O -> 검색결과 없음, hasNext X)")
	void getFollowings_noCursor_noIdAfter_limit5_nameLikeUsed_emptyResult() {
		//given
		String cursor = UUID.randomUUID().toString();
		int limit = 5;
		String nameLike = "zzz";

		// 현재 팔로우 2개
		List<Follow> followList = List.of(
			Follow.builder().follower(follower).followee(followee).build(),
			Follow.builder().follower(follower).followee(followee).build()
		);

		//dto 변환
		List<FollowDto> dtoList = List.of(
			new FollowDto(
				UUID.randomUUID(),
				new UserSummaryDto(followee.getId(), followee.getName(), null),
				new UserSummaryDto(follower.getId(), follower.getName(), null)
			),
			new FollowDto(
				UUID.randomUUID(),
				new UserSummaryDto(followee.getId(), followee.getName(), null),
				new UserSummaryDto(follower.getId(), follower.getName(), null)
			)
		);

		//mock
		when(followRepository.findFollowees(eq(followerId), eq(UUID.fromString(cursor)), eq(nameLike), any()))
			.thenReturn(followList);
		when(followRepository.countByFollowerId(followerId)).thenReturn(0L);
		when(followMapper.toFollowDtoList(followList)).thenReturn(emptyList());

		//when
		FollowListResponse result = followService.getFollowings(
			followerId, cursor, null, limit, nameLike, "id", "ASCENDING");

		//then
		assertNotNull(result); // 결과 not null
		assertEquals(0, result.data().size()); // 데이터 일치
		assertNull(result.nextCursor()); // 다음 커서 존재x
		assertNull(result.nextIdAfter());
		assertFalse(result.hasNext()); // hasNext
		assertEquals(0L, result.totalCount()); // 총 카운트 확인
		assertEquals("id", result.sortBy());
		assertEquals("ASCENDING", result.sortDirection());
	}

	// getFollowers - 유저를 팔로우 하는 사람들 목록 조회(팔로워 클릭)
	@Test
	@DisplayName("getFollowers - 팔로워 목록 조회 성공(cursor X, idAfter X, limit 2, nameLike X, hasNext true)")
	public void getFollowers_noCursor_noIdAfter_limit2_noNameLike() {
		//given
		int limit = 2;

		List<Follow> followList = List.of(
			Follow.builder().follower(follower).followee(followee).build(),
			Follow.builder().follower(follower).followee(followee).build(),
			Follow.builder().follower(follower).followee(followee).build()
		);

		List<FollowDto> dtoList = List.of(
			new FollowDto(UUID.randomUUID(),
				new UserSummaryDto(followee.getId(), followee.getName(), null),
				new UserSummaryDto(follower.getId(), follower.getName(), null)
			),
			new FollowDto(UUID.randomUUID(),
				new UserSummaryDto(followee.getId(), followee.getName(), null),
				new UserSummaryDto(follower.getId(), follower.getName(), null)
			)
		);

		when(followRepository.findFollowers(eq(followeeId), isNull(), isNull(), any()))
			.thenReturn(followList);

		when(followRepository.countByFolloweeId(followeeId)).thenReturn(3L);
		when(followMapper.toFollowDtoList(any())).thenReturn(dtoList);

		//when
		FollowListResponse result = followService.getFollowers(
			followeeId, null, null, limit, null, "id", "ASCENDING"
		);

		//then
		assertNotNull(result);
		assertEquals(2, result.data().size());
		assertTrue(result.hasNext());
		assertNotNull(result.nextCursor());
	}

	@Test
	@DisplayName("getFollowers - 팔로워 목록 조회 성공(cursor O, idAfter X, limit 2, nameLike O, hasNext:false)")
	public void getFollowers_cursorUsed_noIdAfter_limit2_nameLikeUsed() {
		//given
		String cursor = UUID.randomUUID().toString();
		int limit = 2;
		String nameLike = "팔";

		List<Follow> followList = List.of(
			Follow.builder().follower(follower).followee(followee).build(),
			Follow.builder().follower(follower).followee(followee).build()
		);

		List<FollowDto> dtoList = List.of(
			new FollowDto(UUID.randomUUID(),
				new UserSummaryDto(followee.getId(), followee.getName(), null),
				new UserSummaryDto(follower.getId(), follower.getName(), null)
			),
			new FollowDto(UUID.randomUUID(),
				new UserSummaryDto(followee.getId(), followee.getName(), null),
				new UserSummaryDto(follower.getId(), follower.getName(), null)
			)
		);

		when(followRepository.findFollowers(eq(followeeId), eq(UUID.fromString(cursor)), eq(nameLike), any()))
			.thenReturn(followList);
		when(followRepository.countByFolloweeId(followeeId)).thenReturn(2L);
		when(followMapper.toFollowDtoList(followList)).thenReturn(dtoList);

		//when
		FollowListResponse result = followService.getFollowers(
			followeeId, cursor, null, limit, nameLike, "id", "ASCENDING"
		);

		//then
		assertNotNull(result);
		assertEquals(2, result.data().size());
		assertNull(result.nextCursor());
		assertNull(result.nextIdAfter());
		assertFalse(result.hasNext());
		assertEquals(2L, result.totalCount());
		assertEquals("id", result.sortBy());
		assertEquals("ASCENDING", result.sortDirection());
	}

	@Test
	@DisplayName("getFollowers - 팔로워 목록 조회(cursor X, idAfter O, limit 3, nameLike X, hasNext X)")
	public void getFollowers_noCursor_idAfterUsed_limit3_noNameLike() {
		//given
		UUID idAfter = UUID.randomUUID();
		int limit = 3;

		List<Follow> followList = List.of(
			Follow.builder().follower(follower).followee(followee).build(),
			Follow.builder().follower(follower).followee(followee).build()
		);

		List<FollowDto> dtoList = List.of(
			new FollowDto(UUID.randomUUID(),
				new UserSummaryDto(followee.getId(), followee.getName(), null),
				new UserSummaryDto(follower.getId(), follower.getName(), null)
			),
			new FollowDto(UUID.randomUUID(),
				new UserSummaryDto(followee.getId(), followee.getName(), null),
				new UserSummaryDto(follower.getId(), follower.getName(), null)
			)
		);

		when(followRepository.findFollowers(eq(followeeId), eq(idAfter), isNull(), any()))
			.thenReturn(followList);
		when(followRepository.countByFolloweeId(followeeId)).thenReturn(2L);
		when(followMapper.toFollowDtoList(followList)).thenReturn(dtoList);

		//when
		FollowListResponse result = followService.getFollowers(
			followeeId, null, idAfter, limit, null, "id", "ASCENDING"
		);

		//then
		assertNotNull(result);
		assertEquals(2, result.data().size());
		assertNull(result.nextCursor());
		assertNull(result.nextIdAfter());
		assertFalse(result.hasNext());
		assertEquals(2L, result.totalCount());
		assertEquals("id", result.sortBy());
		assertEquals("ASCENDING", result.sortDirection());
	}

	@Test
	@DisplayName("getFollowers - 팔로워 목록 조회(cursor O, idAfter X, limit 5, nameLike O -> 검색결과 없음, hasNext X)")
	void getFollowers_noCursor_noIdAfter_limit5_nameLikeUsed_emptyResult() {
		//given
		String cursor = UUID.randomUUID().toString();
		int limit = 5;
		String nameLike = "zzz";

		List<Follow> followList = List.of();

		// 검색결과 없으므로 dtoList도 empty
		List<FollowDto> dtoList = List.of();

		when(followRepository.findFollowers(eq(followeeId), eq(UUID.fromString(cursor)), eq(nameLike), any()))
			.thenReturn(followList);
		when(followRepository.countByFolloweeId(followeeId)).thenReturn(0L);
		when(followMapper.toFollowDtoList(followList)).thenReturn(dtoList);

		//when
		FollowListResponse result = followService.getFollowers(
			followeeId, cursor, null, limit, nameLike, "id", "ASCENDING"
		);

		//then
		assertNotNull(result);
		assertEquals(0, result.data().size());
		assertNull(result.nextCursor());
		assertNull(result.nextIdAfter());
		assertFalse(result.hasNext());
		assertEquals(0L, result.totalCount());
		assertEquals("id", result.sortBy());
		assertEquals("ASCENDING", result.sortDirection());
	}

	//cancelFollow() - 팔로우 취소
	@Test
	@DisplayName("cancelFollow - 팔로우 취소 성공")
	void cancelFollow_success(){
		//given
		Follow follow = Follow.builder().follower(follower).followee(followee).build();
		when(followRepository.findById(follow.getId())).thenReturn(Optional.of(follow));

		//when
		followService.cancelFollow(follow.getId(),followerId);

		//then
		verify(followRepository, times(1)).deleteById(follow.getId());
	}

	@Test
	@DisplayName("cancelFollow - 존재하지 않는 팔로우인데 취소하면 예외 발생")
	void cancelFollow_fail_followNotFound(){
		//given
		Follow follow = Follow.builder().follower(follower).followee(followee).build();
		when(followRepository.findById(follow.getId())).thenReturn(Optional.empty());

		//when,then
		CustomException ex = assertThrows(CustomException.class, () -> followService.cancelFollow(follow.getId(),followerId));
		assertEquals(ErrorCode.FOLLOW_NOT_FOUND, ex.getErrorCode());
		verify(followRepository, times(0)).deleteById(follow.getId());
	}

	@Test
	@DisplayName("cancelFollow - 팔로워가 아닌 유저가 팔로우 취소 시도하면 예외 발생")
	void cancelFollow_fail_notMyFollow(){
		//given
		Follow follow = Follow.builder().follower(follower).followee(followee).build();
		when(followRepository.findById(follow.getId())).thenReturn(Optional.of(follow));

		//when,then
		CustomException ex = assertThrows(CustomException.class, () -> followService.cancelFollow(follow.getId(),userId));
		assertEquals(ErrorCode.FOLLOW_CANCEL_ONLY_MINE, ex.getErrorCode());
		verify(followRepository, times(0)).deleteById(follow.getId());
	}
}
