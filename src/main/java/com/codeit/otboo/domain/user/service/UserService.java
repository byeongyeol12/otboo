package com.codeit.otboo.domain.user.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.codeit.otboo.domain.user.entity.User;
import com.codeit.otboo.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

	private final UserRepository userRepository;

	public User getUserById(UUID followeeId) {
		return userRepository.findById(followeeId).orElse(null);
	}
}
