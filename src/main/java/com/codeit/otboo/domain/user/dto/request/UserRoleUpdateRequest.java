package com.codeit.otboo.domain.user.dto.request;

import com.codeit.otboo.global.enumType.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "사용자 역할(Role) 변경 요청 DTO")
public record UserRoleUpdateRequest(

		@Schema(
				description = "변경할 역할 (USER: 일반 사용자, ADMIN: 관리자)",
				example = "ADMIN"
		)
		@NotNull
		Role role
) { }
