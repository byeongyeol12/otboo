package com.codeit.otboo.domain.dm.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.codeit.otboo.domain.dm.dto.DirectMessageCreateRequest;
import com.codeit.otboo.domain.dm.dto.DirectMessageDto;
import com.codeit.otboo.domain.dm.dto.DirectMessageDtoCursorResponse;
import com.codeit.otboo.domain.dm.entity.Dm;
import com.codeit.otboo.domain.dm.mapper.DirectMessageMapper;
import com.codeit.otboo.domain.dm.redis.RedisPublisher;
import com.codeit.otboo.domain.dm.repository.DmRepository;
import com.codeit.otboo.domain.dm.util.DmKeyUtil;
import com.codeit.otboo.domain.dm.websocket.NewDmEvent;
import com.codeit.otboo.domain.notification.dto.NotificationDto;
import com.codeit.otboo.domain.notification.entity.NotificationLevel;
import com.codeit.otboo.domain.notification.service.NotificationService;
import com.codeit.otboo.domain.user.entity.User;
import com.codeit.otboo.domain.user.repository.UserRepository;
import com.codeit.otboo.exception.CustomException;
import com.codeit.otboo.global.error.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class DmServiceImpl implements DmService {

	private final UserRepository userRepository;
	private final NotificationService notificationService;
	private final DirectMessageMapper directMessageMapper;
	private final DmRepository dmRepository;
	private final RedisTemplate redisTemplate;
	private final ObjectMapper objectMapper;
	private final RedisPublisher redisPublisher;
	private final ApplicationEventPublisher eventPublisher;

	/**
	 * 유저 검증 -> 메시지 저장 -> DTO 변환 -> Redis Pub/Sub 발행  -> 알림 발송 -> Spring Event 발행
	 * @param directMessageCreateRequest
	 * @return
	 */
	@Override
	public DirectMessageDto sendDirectMessage(DirectMessageCreateRequest directMessageCreateRequest) {
		log.info("[sendDirectMessage] 메시지 생성 시작 : request = {}", directMessageCreateRequest);

		//유저 조회/검증
		User sender = userRepository.findById(directMessageCreateRequest.senderId())
			.orElseThrow(() -> {
				log.warn("[sendDirectMessage] 발신자 조회 실패 : senderId={}", directMessageCreateRequest.senderId());
				return new CustomException(ErrorCode.USER_NOT_FOUND, "발신자를 찾을 수 없습니다.");
			});
		User receiver = userRepository.findById(directMessageCreateRequest.receiverId())
			.orElseThrow(() -> {
				log.warn("[sendDirectMessage] 수신자 조회 실패 : receiverId={}", directMessageCreateRequest.receiverId());
				return new CustomException(ErrorCode.USER_NOT_FOUND, "수신자를 찾을 수 없습니다.");
			});

		//dm 생성 및 저장
		Dm dm = new Dm(sender,receiver,directMessageCreateRequest.content());
		dmRepository.save(dm);
		log.info("[sendDirectMessage] DM 저장 완료 : dmId={}, senderId={}, receiverId={}", dm.getId(), sender.getId(), receiver.getId());

		//dto 변환
		DirectMessageDto directMessageDto = directMessageMapper.toDirectMessageDto(dm);

		// DM Key 생성( = 방 역할)
		String dmKey = DmKeyUtil.makeDmKey(sender.getId(), receiver.getId());

		// Redis pub/sub 발행
		try {
			String messageJson = objectMapper.writeValueAsString(directMessageDto);
			redisPublisher.publish("dm:" + dmKey, messageJson);
			log.info("[sendDirectMessage] Redis publish 성공 : dmKey={}", dmKey);
		} catch (Exception e) {
			log.error("[sendDirectMessage] Redis publish 실패 : dmKey={}, error={}", dmKey, e.getMessage(), e);
			throw new CustomException(ErrorCode.DM_Redis_MESSAGE_ERROR, e.getMessage());
		}

		eventPublisher.publishEvent(new NewDmEvent(directMessageDto));

		// 알림 전송
		try {
			notificationService.createAndSend(
				new NotificationDto(
					UUID.randomUUID(),
					Instant.now(),
					receiver.getId(),
					"DM",
					"[" + sender.getName() + "] 님이 DM 을 보냈습니다.",
					NotificationLevel.INFO
				)
			);
			log.info("[sendDirectMessage] 알림 전송 성공 : receiverId={}", receiver.getId());
		} catch (Exception e) {
			log.error("[sendDirectMessage] 알림 전송 실패 : receiverId={}, error={}", receiver.getId(), e.getMessage(), e);
			throw new CustomException(ErrorCode.NOTIFICATION_CREATE_FAILED);
		}

		return directMessageDto;
	}

	/**
	 * DM 목록을 커서 기반으로 조회
	 * @param userId
	 * @param cursor
	 * @param idAfter
	 * @param limit
	 * @return
	 */
	@Override
	public DirectMessageDtoCursorResponse getDms(UUID userId, UUID otherId, String cursor, UUID idAfter, int limit) {
		// 1. 커서 변환
		UUID effectiveIdAfter = (cursor != null && !cursor.isBlank())
			? UUID.fromString(cursor)
			: idAfter;
		// 2. 정렬(createdAt)
		Pageable pageable = PageRequest.of(0,limit+1, Sort.by("createdAt").ascending());

		// 3. repository
		List<Dm> dms = dmRepository.findAllByUserIdAndOtherIdAfterCursor(userId,otherId,effectiveIdAfter,pageable);

		// 4. hasNext,nextCursor
		boolean hasNext = dms.size() > limit;
		List<Dm> pagedDms = hasNext ? dms.subList(0,limit) : dms;

		String nextCusor = hasNext ? pagedDms.get(pagedDms.size() -1).getId().toString() : null;
		UUID nextIdAfter = hasNext ? pagedDms.get(pagedDms.size() -1).getId() : null;

		//5. dto 변환
		List<DirectMessageDto> directMessageDtoList = pagedDms.stream()
			.map(dm->directMessageMapper.toDirectMessageDto(dm))
			.toList();

		log.info("[getDms] DM 목록 조회 : userId={}, count={}", userId, directMessageDtoList.size());

		return new DirectMessageDtoCursorResponse(
			directMessageDtoList,
			nextCusor,
			nextIdAfter,
			hasNext,
			directMessageDtoList.size(),
			"createdAt",
			"ASCENDING"
		);
	}
}
