package com.codeit.otboo.domain.dm.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.codeit.otboo.domain.dm.entity.Dm;

public interface DmRepository extends JpaRepository<Dm, UUID> {

}
