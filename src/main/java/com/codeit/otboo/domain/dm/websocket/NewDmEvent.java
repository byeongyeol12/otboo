package com.codeit.otboo.domain.dm.websocket;

import java.time.Instant;

import com.codeit.otboo.domain.dm.dto.DirectMessageDto;

public record NewDmEvent(
	Instant createdAt,
	DirectMessageDto dmDto
) {
	public NewDmEvent(DirectMessageDto dmDto){
		this(Instant.now(), dmDto);
	}
}
