package com.codeit.otboo.domain.dm.service;

import com.codeit.otboo.domain.dm.dto.DirectMessageCreateRequest;
import com.codeit.otboo.domain.dm.dto.DirectMessageDto;

public interface DmService {
	DirectMessageDto sendDirectMessage(DirectMessageCreateRequest directMessageCreateRequest);
}
