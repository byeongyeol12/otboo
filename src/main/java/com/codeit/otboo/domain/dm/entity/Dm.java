package com.codeit.otboo.domain.dm.entity;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.codeit.otboo.domain.user.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
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
@Table(name = "direct_messages")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Dm {

	@Id
	@Column(nullable = false, updatable = false)
	private UUID id;

	@CreatedDate
	@Column(name = "created_at",
		nullable = false, updatable = false,
		columnDefinition = "TIMESTAMPTZ")
	private Instant createdAt;


	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "sender_id",   nullable = false)
	private User sender;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "receiver_id", nullable = false)
	private User receiver;

	@Column(columnDefinition = "TEXT", nullable = false)
	private String content;

	@Builder
	public Dm(User sender, User receiver, String content) {
		this.id = UUID.randomUUID();
		this.createdAt = Instant.now();
		this.sender   = sender;
		this.receiver = receiver;
		this.content  = content;
	}
}