package com.codeit.otboo.domain.user.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.codeit.otboo.domain.user.entity.User;

public interface UserRepository extends JpaRepository<User, UUID> {

}
