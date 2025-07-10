package com.codeit.otboo.domain.feed.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.codeit.otboo.domain.feed.entity.Ootd;

public interface OotdRepository extends JpaRepository<Ootd, UUID> {

	@Query("SELECT o FROM Ootd o JOIN FETCH o.clothes WHERE o.feed.id IN :feedIds")
	List<Ootd> findByFeedIdIn(@Param("feedIds") List<UUID> feedIds);
}