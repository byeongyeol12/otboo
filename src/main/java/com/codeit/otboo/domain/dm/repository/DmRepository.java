package com.codeit.otboo.domain.dm.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.codeit.otboo.domain.dm.entity.Dm;

public interface DmRepository extends JpaRepository<Dm, UUID> {
	@Query("SELECT m FROM Dm m WHERE (m.sender.id = :userId OR m.receiver.id = :userId) "
		+ "AND (:idAfter IS NULL OR m.id > :idAfter) "
		+ "ORDER BY m.createdAt ASC")
	List<Dm> findAllByUserIdAfterCursor(UUID userId, UUID idAfter, Pageable pageable);
}
