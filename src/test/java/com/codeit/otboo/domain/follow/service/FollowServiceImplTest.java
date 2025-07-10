package com.codeit.otboo.domain.follow.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.Arrays;
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
import org.springframework.data.domain.Pageable;

import com.codeit.otboo.domain.follow.dto.FollowCreateRequest;
import com.codeit.otboo.domain.follow.dto.FollowDto;
import com.codeit.otboo.domain.follow.dto.FollowListResponse;
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
		follower = new User();
		follower.setId(followerId);
		follower.setName("팔로워");

		followee = new User();
		followee.setId(followeeId);
		followee.setName("팔로이");

		User user = new User();
		user.setId(userId);
		user.setName("유저");
	}

	//createFollow
	@Test
	@DisplayName("createFollow - 팔로우 생성 성공")
	public void createFollow_success() {
		//given
		//FollowCreateRequest 생성
		FollowCreateRequest request = new FollowCreateRequest(followerId, followeeId);

		//팔로워,팔로이 모두 존재한다고 가정
		when(userRepository.findById(followerId)).thenReturn(Optional.of(follower));
		when(userRepository.findById(followeeId)).thenReturn(Optional.of(followee));

		//중복 팔로우 방지
		when(followRepository.existsByFollowerIdAndFolloweeId(followeeId, followerId)).thenReturn(false);

		//팔로우 저장
		Follow follow = Follow.builder().follower(follower).followee(followee).build();
		when(followRepository.save(any(Follow.class))).thenReturn(follow);

		//dto 변환
		FollowDto dto = new FollowDto(
			UUID.randomUUID(),
			new UserSummaryDto(
				followee.getId(),
				followee.getName(),
				null
			),
			new UserSummaryDto(
				follower.getId(),
				follower.getName(),
				null
			));
		when(followMapper.toFollowDto(any(Follow.class))).thenReturn(dto);

		//when
		FollowDto result = followService.createFollow(request);

		//then
		//반환된 dto != null
		assertThat(result).isNotNull();
		//알림 서비스 호출 확인
		then(notificationService).should(times(1))
			.createAndSend(eq(followerId), eq("팔로우"), contains("새 팔로워"), eq(NotificationLevel.INFO));
	}

	@Test
	@DisplayName("createFollow - 실패 : 자기 자신 팔로우")
	public void createFollow_fail_follow_myself() {
		//given

		//FollowCreateRequest 생성
		FollowCreateRequest request = new FollowCreateRequest(followerId, followerId);

		//팔로워만 존재
		when(userRepository.findById(followerId)).thenReturn(Optional.of(follower));

		//when,then
		CustomException ex = assertThrows(CustomException.class, () -> followService.createFollow(request));
		assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.FOLLOW_NOT_MYSELF);
	}

	@Test
	@DisplayName("createFollow - 실패 : 팔로우 중복 발생")
	public void createFollow_fail_follow_duplicated() {
		//given
		//FollowCreateRequest 생성
		FollowCreateRequest request = new FollowCreateRequest(followerId, followeeId);

		//팔로워,팔로이 모두 존재한다고 가정
		when(userRepository.findById(followerId)).thenReturn(Optional.of(follower));
		when(userRepository.findById(followeeId)).thenReturn(Optional.of(followee));

		//중복 발생
		when(followRepository.existsByFollowerIdAndFolloweeId(any(), any())).thenReturn(true);

		//when,then
		CustomException ex = assertThrows(CustomException.class, () -> followService.createFollow(request));
		assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.FOLLOW_ALREADY_USER);
	}

	//getFollowSummary - 팔로우 요약 정보 조회

	// getFollowings - 	유저가 팔로우 하는 사람들 목록 조회(팔로우 클릭)
	@Test
	@DisplayName("getFollowings - 팔로잉 목록 조회 성공(커서X, idAfterX, limit 미만 반환)")
	public void getFollowing_success_underLimit() {
		//given
		String cursor = null;
		UUID idAfter = null;
		int limit = 3;
		String nameLike = null;
		String sortBy = null; //id
		String sortDirection = null; //ASC

		// 현재 팔로우 2개(limit 3)
		List<Follow> followList = Arrays.asList(
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

		//repository,mapper mock
		// 팔로우 목록 리턴
		when(followRepository.findFollowees(eq(followerId), isNull(), eq(nameLike), any(Pageable.class)))
			.thenReturn(followList);

		// 총 팔로잉 수 조회
		when(followRepository.countByFollowerId(followerId)).thenReturn(2L);
		when(followMapper.toFollowDtoList(anyList())).thenReturn(dtoList);

		//when
		FollowListResponse response = followService.getFollowings(
			followerId, cursor, idAfter, limit, nameLike, sortBy, sortDirection
		);

		//then
		assertThat(response.data()).hasSize(2); // 반환된 목록 개수 2개 
		assertThat(response.hasNext()).isFalse(); // 다음 데이터가 없음
		assertThat(response.totalCount()).isEqualTo(2L); //전체 팔로잉 수 2명
		assertThat(response.sortBy()).isEqualTo(sortBy);
	}

	@Test
	@DisplayName("getFollowings - 커서 기반 페이징 성공")
	public void getFollowings_success_cursorPaging(){
		//given
		int limit = 2;
		String cursor = UUID.randomUUID().toString();
		UUID idAfter = UUID.randomUUID();
		String nameLike = "팔";
		String sortBy = "createdAt";
		String sortDirection = "DESCENDING";

		//커서 변환
		UUID effectiveIdAfter = UUID.fromString(cursor);

		// 현재 팔로우 3개(limit +1 개)
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
			,
			new FollowDto(
				UUID.randomUUID(),
				new UserSummaryDto(followee.getId(), followee.getName(), null),
				new UserSummaryDto(follower.getId(), follower.getName(), null)
			)
		);

		//repository, mapper mock
		when(followRepository.findFollowees(eq(followerId), eq(effectiveIdAfter), eq(nameLike), any(Pageable.class)))
			.thenReturn(followList);
		when(followRepository.countByFollowerId(followerId)).thenReturn(10L);
		when(followMapper.toFollowDtoList(followList)).thenReturn(dtoList);


		//when
		FollowListResponse result = followService.getFollowings(
			followerId, cursor, null, limit, nameLike, sortBy, sortDirection);

		//then
		assertNotNull(result); // 결과 not null
		assertEquals(dtoList, result.data()); // 데이터 일치
		assertTrue(result.hasNext()); // hasNext = true
		assertEquals(10L, result.totalCount()); // 총 카운트 확인
		assertEquals(sortBy, result.sortBy());
		assertEquals(sortDirection, result.sortDirection());
		assertNotNull(result.nextCursor()); // 다음 커서 존재
		assertNotNull(result.nextIdAfter());
		assertEquals(followList.get(limit - 1).getId(), result.nextIdAfter()); // 다음 커서는 2번째(0,1번째)의 id
	}

	@Test
	@DisplayName("getFollowings - idAfer만 사용")
	public void getFollowings_success_cursorPagingIdAfter(){
		//given
		int limit = 1;
		String cursor = null; // 커서 없음
		UUID idAfter = UUID.randomUUID();
		String nameLike = null;
		String sortBy = null; // default: "id"
		String sortDirection = null; // default: "ASC"

		// 현재 팔로우 2개(limit +1 개)
		List<Follow> followList = List.of(
			Follow.builder().follower(follower).followee(followee).build(),
			Follow.builder().follower(follower).followee(followee).build()
		);

		//dto 변환
		List<FollowDto> dtoList = List.of(
			new FollowDto(
				UUID.randomUUID(),
				new UserSummaryDtoDto(followee.getId(), followee.getName(), null),
				new UserSummaryDtoDto(follower.getId(), follower.getName(), null)
			),
			new FollowDto(
				UUID.randomUUID(),
				new UserSummaryDtoDto(followee.getId(), followee.getName(), null),
				new UserSummaryDtoDto(follower.getId(), follower.getName(), null)
			)
		);

		// Mock 세팅
		when(followRepository.findFollowees(eq(followerId), eq(idAfter), any(), any(Pageable.class)))
			.thenReturn(followList); // followRepository가 3개 반환
		when(followRepository.countByFollowerId(followerId)).thenReturn(10L); // 총 follow 수
		when(followMapper.toFollowDtoList(followList)).thenReturn(dtoList); // 3개 dto 반환


		//when
		FollowListResponse result = followService.getFollowings(
			followerId, cursor, idAfter, limit, nameLike, sortBy, "ASCENDING");

		// then
		assertNotNull(result); // 결과값 null 아님
		assertEquals(2, result.data().size()); // limit만큼(2개) 반환
		assertTrue(result.hasNext()); // hasNext = true (3개 중 2개만 반환)
		assertNotNull(result.nextCursor()); // 다음 커서 값 있음
		assertNotNull(result.nextIdAfter()); // 다음 idAfter 값 있음
		assertEquals(followList.get(limit-1).getId(), result.nextIdAfter()); // 2번째 follow의 id가 커서로 반환됨
		assertEquals(null, result.sortBy()); // default 정렬 필드
		assertEquals("ASCENDING", result.sortDirection()); // default 정렬 방향
	}

	// getFollowers - 유저를 팔로우 하는 사람들 목록 조회(팔로워 클릭)
	@Test
	@DisplayName("getFollowers - 팔로워 목록 조회 성공(커서X, idAfterX, limit 미만 반환)")
	public void getFollowers_success_underLimit(){
		//given
		String cursor = null;
		UUID idAfter = null;
		int limit = 3;
		String nameLike = null;
		String sortBy = null; //id
		String sortDirection = null; //ASC

		// 현재 팔로우 2개(limit 3)
		List<Follow> followList = Arrays.asList(
			Follow.builder().follower(follower).followee(followee).build(),
			Follow.builder().follower(follower).followee(followee).build()
		);

		//dto 변환
		List<FollowDto> dtoList = List.of(
			new FollowDto(
				UUID.randomUUID(),
				new UserSummaryDtoDto(followee.getId(), followee.getName(), null),
				new UserSummaryDtoDto(follower.getId(), follower.getName(), null)
			),
			new FollowDto(
				UUID.randomUUID(),
				new UserSummaryDtoDto(followee.getId(), followee.getName(), null),
				new UserSummaryDtoDto(follower.getId(), follower.getName(), null)
			)
		);

		//repository,mapper mock
		// 팔로우 목록 리턴
		when(followRepository.findFollowers(eq(followeeId), isNull(), eq(nameLike), any(Pageable.class)))
			.thenReturn(followList);

		// 총 팔로잉 수 조회
		when(followRepository.countByFolloweeId(followeeId)).thenReturn(2L);
		when(followMapper.toFollowDtoList(anyList())).thenReturn(dtoList);

		//when
		FollowListResponse response = followService.getFollowers(
			followeeId, cursor, idAfter, limit, nameLike, sortBy, sortDirection
		);

		//then
		assertThat(response.data()).hasSize(2); // 반환된 목록 개수 2개
		assertThat(response.hasNext()).isFalse(); // 다음 데이터가 없음
		assertThat(response.totalCount()).isEqualTo(2L); //전체 팔로잉 수 2명
		assertThat(response.sortBy()).isEqualTo(sortBy);
	}
}
