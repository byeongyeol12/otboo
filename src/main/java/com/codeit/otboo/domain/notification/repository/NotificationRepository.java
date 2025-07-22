package com.codeit.otboo.domain.notification.repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.codeit.otboo.domain.notification.entity.Notification;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

	List<Notification> findByReceiverIdAndConfirmedFalseAndCreatedAtLessThanOrderByCreatedAtDesc(UUID receiverId, Instant createdAtIsLessThan, Pageable attr0);

	List<Notification> findByReceiverIdAndConfirmedFalseOrderByCreatedAtDesc(UUID receiverId, Pageable attr0);

	long countByReceiverIdAndConfirmedFalse(UUID receiverId);
}
