package com.codeit.otboo.domain.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "로그인 요청 DTO")
public class LoginRequest {

	@Schema(description = "이메일", example = "user@example.com")
	@Email
	@NotBlank
	private String email;

	@Schema(description = "비밀번호", example = "password123!")
	@NotBlank
	private String password;

	protected LoginRequest() {
	}

	public LoginRequest(String email, String password) {
		this.email = email;
		this.password = password;
	}

	public String getEmail() {
		return email;
	}

	public String getPassword() {
		return password;
	}
}
