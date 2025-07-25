package com.codeit.otboo.domain.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "CSRF 토큰 응답 DTO")
public record CsrfTokenResponse(

		@Schema(description = "CSRF 헤더 이름", example = "X-CSRF-TOKEN")
		String headerName,

		@Schema(description = "CSRF 토큰 값", example = "a1b2c3d4e5f6g7h8i9j0")
		String token,

		@Schema(description = "CSRF 파라미터 이름", example = "_csrf")
		String parameterName

) { }
