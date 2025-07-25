package com.codeit.otboo.domain.user.dto.request;

import com.codeit.otboo.global.enumType.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "사용자 검색 및 페이징 요청 DTO")
public class UserSearchRequest {

	@Schema(description = "페이지네이션 커서")
	private String cursor;

	@Schema(description = "특정 ID 이후의 데이터를 조회하기 위한 기준 ID")
	private UUID idAfter;

	@Schema(description = "한 페이지에 보여줄 항목 수", defaultValue = "10")
	private Integer limit;

	@Schema(description = "정렬 기준 필드", defaultValue = "createdAt")
	private String sortBy;

	@Schema(description = "정렬 방향 (ASC or DESC)", defaultValue = "DESC")
	private String sortDirection;

	@Schema(description = "검색할 이메일 키워드 (부분 일치)")
	private String emailLike;

	@Schema(description = "검색할 사용자 권한 (USER or ADMIN)")
	private Role roleEqual;

	@Schema(description = "계정 잠금 상태로 검색")
	private Boolean locked;

	public UserSearchRequest() {
	}

	public Integer limit() {
		return limit;
	}

	public String sortBy() {
		return sortBy;
	}

	public String sortDirection() {
		return sortDirection;
	}

	public String cursor() {
		return cursor;
	}

	public UUID idAfter() {
		return idAfter;
	}

	public String emailLike() {
		return emailLike;
	}

	public Role roleEqual() {
		return roleEqual;
	}

	public Boolean locked() {
		return locked;
	}

	public void setLimit(Integer limit) {
		this.limit = limit;
	}

	public void setSortBy(String sortBy) {
		this.sortBy = sortBy;
	}

	public void setSortDirection(String sortDirection) {
		this.sortDirection = sortDirection;
	}

	public void setCursor(String cursor) {
		this.cursor = cursor;
	}

	public void setIdAfter(UUID idAfter) {
		this.idAfter = idAfter;
	}

	public void setEmailLike(String emailLike) {
		this.emailLike = emailLike;
	}

	public void setRoleEqual(Role roleEqual) {
		this.roleEqual = roleEqual;
	}

	public void setLocked(Boolean locked) {
		this.locked = locked;
	}
}