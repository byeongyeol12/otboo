package com.codeit.otboo.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "회원가입 요청 DTO")
public record UserCreateRequest(

		@Schema(description = "사용자 이름", example = "홍길동")
		@NotBlank(message = "이름은 필수 입력 항목입니다.")
		@Size(max = 50, message = "이름은 50자 이하로 입력해야 합니다.")
		String name,

		@Schema(description = "사용자 이메일", example = "test@example.com")
		@NotBlank(message = "이메일은 필수 입력 항목입니다.")
		@Email(message = "유효한 이메일 형식이 아닙니다.")
		String email,

		@Schema(description = "비밀번호 (영문, 숫자, 특수문자 포함 8~20자)", example = "password123!")
		@NotBlank(message = "비밀번호는 필수 입력 항목입니다.")
		@Size(min = 8, max = 20, message = "비밀번호는 8자 이상 20자 이하로 입력해야합니다.")
		@Pattern(
				regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,20}$",
				message = "비밀번호는 영문자, 숫자, 특수문자를 포함한 8~20자로 설정해주세요.")
		String password
) {
}
