package com.codeit.otboo.domain.notification.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.codeit.otboo.domain.notification.entity.Notification;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

}
