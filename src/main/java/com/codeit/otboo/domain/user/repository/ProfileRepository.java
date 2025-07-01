package com.codeit.otboo.domain.user.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.codeit.otboo.domain.user.entity.Profile;

public interface ProfileRepository extends JpaRepository<Profile, UUID> {
	Optional<Profile> findByUserId(UUID userId);
}
