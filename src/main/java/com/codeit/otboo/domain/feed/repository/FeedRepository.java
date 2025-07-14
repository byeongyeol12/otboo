package com.codeit.otboo.domain.feed.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.codeit.otboo.domain.feed.entity.Feed;

@Repository
public interface FeedRepository
	extends JpaRepository<Feed, UUID>, FeedRepositoryCustom {
}
