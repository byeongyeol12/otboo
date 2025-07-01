package com.codeit.otboo.domain.user.repository;

import java.util.List;

import com.codeit.otboo.domain.user.dto.request.UserSearchRequest;
import com.codeit.otboo.domain.user.entity.User;

public interface UserSearchRepository {
	List<User> search(UserSearchRequest request);

	long count(UserSearchRequest request);
}
