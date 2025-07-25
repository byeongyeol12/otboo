package com.codeit.otboo.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "비밀번호 변경 요청 DTO")
public record PasswordRequest(
		@Schema(description = "새로운 비밀번호", example = "newPassword123!")
		@NotBlank(message = "새 비밀번호는 필수입니다.")
		@Size(min = 8, max = 20, message = "비밀번호는 8자 이상 20자 이하여야 합니다.")
		String newPassword
) {
}