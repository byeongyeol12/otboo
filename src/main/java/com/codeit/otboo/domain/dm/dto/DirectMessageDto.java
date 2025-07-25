package com.codeit.otboo.domain.dm.dto;

import com.codeit.otboo.domain.user.dto.response.UserSummaryDto;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "쪽지(Direct Message) DTO")
public record DirectMessageDto(

		@Schema(description = "메시지 ID", example = "e5cd3d2f-95e2-4d33-b6f7-45b6f4a845ed")
		UUID id,

		@Schema(description = "메시지 생성 시각(UTC ISO8601)", example = "2024-07-25T15:43:12Z")
		Instant createdAt,

		@Schema(description = "발신자 요약 정보")
		UserSummaryDto sender,

		@Schema(description = "수신자 요약 정보")
		UserSummaryDto receiver,

		@Schema(description = "메시지 내용", example = "안녕하세요! 쪽지 보냅니다.")
		String content

) { }
