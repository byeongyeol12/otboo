package com.codeit.otboo.domain.dm.service;

import java.util.UUID;

import com.codeit.otboo.domain.dm.dto.DirectMessageCreateRequest;
import com.codeit.otboo.domain.dm.dto.DirectMessageDto;
import com.codeit.otboo.domain.dm.dto.DirectMessageDtoCursorResponse;

public interface DmService {
	DirectMessageDto sendDirectMessage(DirectMessageCreateRequest directMessageCreateRequest);

	DirectMessageDtoCursorResponse getDms(UUID userId,UUID otherId,String cursor, UUID idAfter, int limit);
}
