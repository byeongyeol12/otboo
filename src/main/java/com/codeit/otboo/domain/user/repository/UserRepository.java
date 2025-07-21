package com.codeit.otboo.domain.user.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.codeit.otboo.domain.user.entity.User;

public interface UserRepository extends JpaRepository<User, UUID>, UserSearchRepository {
	Optional<User> findByEmail(String email);

	boolean existsByEmail(String email);

	User getUserById(UUID followeeId);

	List<User> findByProfileXAndProfileY(int x, int y);

	User findByName(String name);
}

