package com.codeit.otboo.domain.dm.dto;

import java.time.Instant;
import java.util.UUID;

import com.codeit.otboo.domain.user.dto.response.UserSummaryDto;

public record DirectMessageDto(
	UUID id, // 메시지 ID
	Instant createdAt, // 메시지 생성 시간
	UserSummaryDto sender, // 발신자 정보
	UserSummaryDto receiver, // 수신자 정보
	String content // 메시지 내용
) {
}
