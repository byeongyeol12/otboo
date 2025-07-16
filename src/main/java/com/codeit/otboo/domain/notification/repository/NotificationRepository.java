package com.codeit.otboo.domain.notification.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.codeit.otboo.domain.notification.entity.Notification;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {
	List<Notification> findByReceiverIdAndIdGreaterThanOrderByCreatedAt(UUID receiverId, UUID lastEventId);
	List<Notification> findByReceiverIdAndConfirmedFalse(UUID receiverId);
	List<Notification> findByReceiverIdAndIdGreaterThanOrderByCreatedAt(UUID receiverId,UUID idAfter, Pageable pageable);
	List<Notification> findByReceiverIdAndConfirmedFalse(UUID receiverId, Pageable pageable);
	long countByReceiverId(UUID receiverId);
	boolean existsByReceiver_IdAndEventRefIdAndConfirmedFalse(UUID receiverId, UUID eventRefId);

	List<Notification> findByReceiverIdAndConfirmedFalseOrderByCreatedAt(UUID receiverId);
}
