package com.codeit.otboo.domain.sse.util;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SseMessage {
	private UUID eventId;
	private Set<UUID> receiverIds = new HashSet<>();
	private boolean broadcast;
	private String eventName;
	private Object eventData;

	public static SseMessage create(UUID receiverId, String eventName, Object eventData) {
		return new SseMessage(
			UUID.randomUUID(),
			Set.of(receiverId),
			false,
			eventName,
			eventData
		);
	}

	public boolean isReceivable(UUID receiverId) {
		return broadcast || receiverIds.contains(receiverId);
	}

	public Set<ResponseBodyEmitter.DataWithMediaType> toEvent() {
		return SseEmitter.event()
			.id(eventId.toString())
			.name(eventName)
			.data(eventData)
			.build();
	}
}
