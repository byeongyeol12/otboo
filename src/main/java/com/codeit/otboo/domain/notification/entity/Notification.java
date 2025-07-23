package com.codeit.otboo.domain.notification.entity;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.codeit.otboo.domain.user.entity.User;
import com.codeit.otboo.exception.CustomException;
import com.codeit.otboo.global.error.ErrorCode;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "notifications")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Notification {

	@Id
	@Column(nullable = false)
	private UUID id;

	@Column(nullable = false, length = 100)
	private String title;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String content;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false,length = 10)
	private NotificationLevel level;

	@CreatedDate
	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(nullable = false)
	private boolean confirmed = false; //읽음 여부

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="user_id",nullable = false)
	private User receiver; // 알림 대상(수신자)

	@Builder
	public Notification(User receiver, String title, String content, NotificationLevel level,boolean confirmed) {
		this.id = UUID.randomUUID();
		this.receiver = receiver;
		this.title = title;
		this.content = content;
		this.level = level;
		this.confirmed = confirmed;
		this.createdAt = Instant.now();
	}

	// confirmed false -> true
	public void confirmedChange(){
		//true -> 예외
		if(this.confirmed){
			throw new CustomException(ErrorCode.NOTIFICATION_ALREADY_READ);
		}
		this.confirmed = true;
	}
}
