package com.codeit.otboo.domain.dm.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "쪽지(Direct Message) 생성 요청 DTO")
public record DirectMessageCreateRequest(

		@Schema(description = "쪽지 수신자 ID", example = "b68e6784-20de-4b94-a581-832773cf2d50")
		UUID receiverId,

		@Schema(description = "쪽지 발신자 ID", example = "4ec83d6b-48f4-4a53-82de-0bb81ab838e7")
		UUID senderId,

		@Schema(description = "쪽지 내용", example = "안녕하세요! 문의드립니다.")
		String content

) { }
