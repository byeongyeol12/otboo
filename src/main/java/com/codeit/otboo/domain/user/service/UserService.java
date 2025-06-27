package com.codeit.otboo.domain.user.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.codeit.otboo.domain.follow.entity.User;
import com.codeit.otboo.domain.user.repository.UserRepository;

@Service
public class UserService {

	private final UserRepository userRepository;

	public UserService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	public User getUserById(UUID followeeId) {
		return userRepository.findById(followeeId).orElse(null);
	}
}
