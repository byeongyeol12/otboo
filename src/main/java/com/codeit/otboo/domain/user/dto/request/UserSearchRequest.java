package com.codeit.otboo.domain.user.dto.request;

import java.util.UUID;

import com.codeit.otboo.global.enumType.Role;

public class UserSearchRequest {

	private String cursor;
	private UUID idAfter;
	private Integer limit;
	private String sortBy;
	private String sortDirection;
	private String emailLike;
	private Role roleEqual;
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
