package com.codeit.otboo.domain.notification.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.codeit.otboo.domain.notification.entity.Notification;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {
	// 페이징/정렬
	List<Notification> findByReceiverIdAndIdGreaterThanOrderByCreatedAt(UUID receiverId, UUID idAfter, Pageable pageable);
	List<Notification> findByReceiverIdAndConfirmedFalse(UUID receiverId, Pageable pageable);

	// 전체 개수
	long countByReceiverId(UUID receiverId);

}
