package com.codeit.otboo.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "사용자 잠금 상태 변경 요청 DTO")
public record UserLockRequest(

		@Schema(description = "잠금 여부 (true: 잠금, false: 해제)", example = "true")
		boolean locked
) { }
