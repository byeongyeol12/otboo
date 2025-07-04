package com.codeit.otboo.domain.dm.service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.codeit.otboo.domain.dm.dto.DirectMessageCreateRequest;
import com.codeit.otboo.domain.dm.dto.DirectMessageDto;
import com.codeit.otboo.domain.dm.dto.DirectMessageDtoCursorResponse;
import com.codeit.otboo.domain.dm.entity.Dm;
import com.codeit.otboo.domain.dm.mapper.DirectMessageMapper;
import com.codeit.otboo.domain.dm.repository.DmRepository;
import com.codeit.otboo.domain.notification.dto.NotificationDto;
import com.codeit.otboo.domain.notification.entity.NotificationLevel;
import com.codeit.otboo.domain.notification.service.NotificationService;
import com.codeit.otboo.domain.user.entity.User;
import com.codeit.otboo.domain.user.repository.UserRepository;
import com.codeit.otboo.exception.CustomException;
import com.codeit.otboo.global.error.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DmServiceImpl implements DmService {

	private final UserRepository userRepository;
	private final SimpMessagingTemplate simpMessagingTemplate;
	private final NotificationService notificationService;
	private final DirectMessageMapper directMessageMapper;
	private final DmRepository dmRepository;

	@Override
	public DirectMessageDto sendDirectMessage(DirectMessageCreateRequest directMessageCreateRequest) {
		//유저 체크
		User sender = userRepository.findById(directMessageCreateRequest.senderId()).orElseThrow(() -> new CustomException(
			ErrorCode.USER_NOT_FOUND,"발신자를 찾을 수 없습니다."));
		User receiver = userRepository.findById(directMessageCreateRequest.receiverId()).orElseThrow(() -> new CustomException(
			ErrorCode.USER_NOT_FOUND,"수신자를 찾을 수 없습니다."));

		//dm 생성 및 저장
		Dm dm = new Dm(sender,receiver,directMessageCreateRequest.content());
		dmRepository.save(dm);

		DirectMessageDto directMessageDto = directMessageMapper.toDirectMessageDto(dm);

		// 실시간 전송
		String dmKey = makeDmKey(sender.getId,receiver.getId());
		simpMessagingTemplate.convertAndSend("/sub/direct-messages_"+dmKey, directMessageDto);

		// 알림 전송
		notificationService.createAndSend(new NotificationDto(
			UUID.randomUUID(),
			Instant.now(),
			receiver.getId(),
			"DM",
			"["+sender.getName()+"] 님이 DM 을 보냈습니다.",
			NotificationLevel.INFO
		));

		return directMessageDto;
	}

	@Override
	public DirectMessageDtoCursorResponse getDms(UUID userId, String cursor, UUID idAfter, int limit) {
		// 1. 커서 변환
		UUID effectiveIdAfter = (cursor != null && !cursor.isBlank())
			? UUID.fromString(cursor)
			: idAfter;
		// 2. 정렬(createdAt)
		Pageable pageable = PageRequest.of(0,limit+1, Sort.by("createdAr").ascending());

		// 3. repository
		List<Dm> dms = dmRepository.findAllByUserIdAfterCursor(userId,effectiveIdAfter,pageable);

		// 4. hasNext,nextCursor
		boolean hasNext = dms.size() > limit;
		List<Dm> pagedDms = hasNext ? dms.subList(0,limit) : dms;

		String nextCusor = hasNext ? pagedDms.get(pagedDms.size() -1).getId().toString() : null;
		UUID nextIdAfter = hasNext ? pagedDms.get(pagedDms.size() -1).getId() : null;

		//5. dto 변환
		List<DirectMessageDto> directMessageDtoList = pagedDms.stream()
			.map(dm->directMessageMapper.toDirectMessageDto(dm))
			.toList();

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

	private String makeDmKey(UUID senderId, UUID receiverId) {
		List<UUID> ids = List.of(senderId, receiverId);
		ids.sort(Comparator.naturalOrder()); //오름차순 정렬
		return ids.get(0)+"_"+ids.get(1);
	}
}
