package com.codeit.otboo.domain.auth.dto.response;

public record CsrfTokenResponse(
	String headerName,
	String token,

	String parameterName

) {
}
