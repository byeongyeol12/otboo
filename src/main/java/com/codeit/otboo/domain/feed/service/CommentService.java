package com.codeit.otboo.domain.feed.service;

import com.codeit.otboo.domain.feed.dto.request.CommentCreateRequest;
import com.codeit.otboo.domain.feed.dto.response.CommentDto;
import com.codeit.otboo.domain.feed.dto.response.CommentDtoCursorResponse;
import java.time.Instant;
import java.util.UUID;

public interface CommentService {

	CommentDto createComment(UUID feedId ,CommentCreateRequest request);

	CommentDtoCursorResponse listByCursor(UUID feedId, Instant cursor, UUID idAfter,
		int limit);

}
