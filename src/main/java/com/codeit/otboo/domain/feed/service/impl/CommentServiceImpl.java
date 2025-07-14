package com.codeit.otboo.domain.feed.service.impl;

import static com.codeit.otboo.global.error.ErrorCode.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.codeit.otboo.domain.feed.dto.request.CommentCreateRequest;
import com.codeit.otboo.domain.feed.dto.response.CommentDto;
import com.codeit.otboo.domain.feed.dto.response.CommentDtoCursorResponse;
import com.codeit.otboo.domain.feed.entity.Feed;
import com.codeit.otboo.domain.feed.entity.FeedComment;
import com.codeit.otboo.domain.feed.repository.FeedCommentRepository;
import com.codeit.otboo.domain.feed.repository.FeedRepository;
import com.codeit.otboo.domain.feed.service.CommentService;
import com.codeit.otboo.domain.user.entity.User;
import com.codeit.otboo.domain.user.repository.UserRepository;
import com.codeit.otboo.exception.CustomException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {
	private final FeedRepository feedRepository;
	private final UserRepository userRepository;
	private final FeedCommentRepository feedCommentRepository;

	@Transactional
	@Override
	public CommentDto createComment(UUID feedId , CommentCreateRequest request) {
		Feed feed = feedRepository.findById(feedId)
			.orElseThrow(() -> new CustomException(FEED_NOT_FOUND));
		User author = userRepository.findById(request.getAuthorId())
			.orElseThrow(() -> new CustomException(USER_NOT_FOUND));

		FeedComment comment = new FeedComment(feed, author, request.getContent());
		feed.addComment();
		feedRepository.save(feed);
		feedCommentRepository.save(comment);

		return CommentDto.fromEntity(comment);

	}

	@Transactional(readOnly = true)
	@Override
	public CommentDtoCursorResponse listByCursor(
		UUID feedId,
		Instant cursor,
		UUID idAfter,
		int limit
	) {
		List<FeedComment> comments = feedCommentRepository.findByFeedIdCursor(
			feedId, cursor, idAfter, limit + 1
		);

		boolean hasNext = comments.size() > limit;
		List<FeedComment> pageItems = hasNext
			? comments.subList(0, limit)
			: comments;

		List<CommentDto> data = pageItems.stream()
			.map(CommentDto::fromEntity)
			.toList();

		Instant nextCursor = null;
		UUID nextIdAfter = null;

		if (hasNext) {
			FeedComment last = pageItems.get(pageItems.size() - 1);
			nextCursor = last.getCreatedAt();
			nextIdAfter = last.getId();
		}

		long totalCount = feedCommentRepository.countByFeedId(feedId);

		return new CommentDtoCursorResponse(data, nextCursor, nextIdAfter, hasNext, totalCount, "createdAt", "DESCENDING");
	}
}
