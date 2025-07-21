package com.codeit.otboo.domain.dm.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.codeit.otboo.domain.dm.entity.Dm;

public interface DmRepository extends JpaRepository<Dm, UUID> {
	/**
	 * DM 조회/저장 (커서 기반 페이지네이션)
	 * @param userId
	 * @param idAfter
	 * @param pageable
	 * @return
	 */
	@Query("SELECT m FROM Dm m WHERE " +
		"((m.sender.id = :userId AND m.receiver.id = :otherId) OR " +
		"(m.sender.id = :otherId AND m.receiver.id = :userId)) " +
		"AND (:idAfter IS NULL OR m.id > :idAfter) " +
		"ORDER BY m.createdAt ASC")
	List<Dm> findAllByUserIdAndOtherIdAfterCursor(
		UUID userId, UUID otherId, UUID idAfter, Pageable pageable);

}
