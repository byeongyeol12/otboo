package com.codeit.otboo.domain.feed.controller;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.codeit.otboo.domain.feed.dto.request.CommentCreateRequest;
import com.codeit.otboo.domain.feed.dto.request.FeedCreateRequest;
import com.codeit.otboo.domain.feed.dto.request.FeedUpdateRequest;
import com.codeit.otboo.domain.feed.dto.response.CommentDto;
import com.codeit.otboo.domain.feed.dto.response.CommentDtoCursorResponse;
import com.codeit.otboo.domain.feed.dto.response.FeedDto;
import com.codeit.otboo.domain.feed.dto.response.FeedDtoCursorResponse;
import com.codeit.otboo.domain.feed.service.CommentService;
import com.codeit.otboo.domain.feed.service.FeedLikeService;
import com.codeit.otboo.domain.feed.service.FeedService;
import com.codeit.otboo.domain.weather.entity.vo.PrecipitationType;
import com.codeit.otboo.domain.weather.entity.vo.SkyStatus;
import com.codeit.otboo.global.config.security.UserPrincipal;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/feeds")
@RequiredArgsConstructor
public class FeedController {
	private final FeedService feedService;
	private final FeedLikeService feedLikeService;
	private final CommentService commentService;


	@PostMapping
	public ResponseEntity<FeedDto> createFeed(@RequestBody @Valid FeedCreateRequest request) {
		FeedDto feed = feedService.createFeed(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(feed);
	}

	@GetMapping
	public ResponseEntity<FeedDtoCursorResponse> getFeeds(
		@RequestParam(name = "cursor", required = false) String cursor,
		@RequestParam(name = "idAfter", required = false) UUID idAfter,
		@RequestParam(name = "limit", defaultValue = "20") int limit,
		@RequestParam(name = "sortBy", defaultValue = "createdAt") String sortBy,
		@RequestParam(name = "sortDirection", defaultValue = "DESCENDING") String sortDirection,
		@RequestParam(name = "keywordLike", required = false) String keywordLike,
		@RequestParam(name = "skyStatusEqual", required = false) SkyStatus skyStatusEqual,
		@RequestParam(name = "precipitationTypeEqual", required = false) PrecipitationType precipitationTypeEqual,
		@RequestParam(name = "authorIdEqual", required = false) UUID authorIdEqual) {
		Instant cursorCreatedAt = null;
		Long cursorLikeCount = null;
		if (cursor != null && !cursor.isBlank()) {
			try {
				if ("createdAt".equals(sortBy)) {
					cursorCreatedAt = Instant.parse(cursor);
				} else if ("likeCount".equals(sortBy)) {
					cursorLikeCount = Long.valueOf(cursor);
				} else {
					throw new ResponseStatusException(
						HttpStatus.BAD_REQUEST,
						"피드_지원하지 않는 기준타입" + sortBy
					);
				}
			} catch (DateTimeParseException dtype) {
				throw new ResponseStatusException(
					HttpStatus.BAD_REQUEST,
					"최신순 정렬 기준 날짜 변환 실패" + cursor
				);
			} catch (NumberFormatException nfe) {
				throw new ResponseStatusException(
					HttpStatus.BAD_REQUEST,
					"좋아요 순 기준 타입 변환 실패" + cursor
				);
			}
		}
		FeedDtoCursorResponse response = feedService.listByCursor(
			cursorCreatedAt,
			cursorLikeCount,
			idAfter,
			limit,
			sortBy,
			sortDirection,
			keywordLike,
			skyStatusEqual,
			precipitationTypeEqual,
			authorIdEqual
		);
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	@PatchMapping("/{feedId}")
	public ResponseEntity<FeedDto> updateFeed(
		@PathVariable UUID feedId,
		@RequestBody @Valid FeedUpdateRequest request) {
		FeedDto feed = feedService.updateFeed(feedId, request);
		return ResponseEntity.status(HttpStatus.OK).body(feed);
	}

	@DeleteMapping("/{feedId}")
	public ResponseEntity<Void> deleteFeed(@PathVariable UUID feedId) {
		feedService.deleteFeed(feedId);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}

	@PostMapping("/{feedId}/like")
	public ResponseEntity<FeedDto> likeFeed(
		@PathVariable UUID feedId,
		@AuthenticationPrincipal UserPrincipal userPrincipal
	) {
		UUID myUserId = userPrincipal.getId();
		feedLikeService.likeFeed(myUserId, feedId);
		return ResponseEntity.status(HttpStatus.OK).body(feedService.getFeed(feedId));
	}

	@DeleteMapping("/{feedId}/like")
	public ResponseEntity<Void> unlikeFeed(
		@PathVariable UUID feedId,
		@AuthenticationPrincipal UserPrincipal userPrincipal
	) {
		UUID myUserId = userPrincipal.getId();
		feedLikeService.unlikeFeed(myUserId, feedId);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}

	@PostMapping("/{feedId}/comments")
	public ResponseEntity<CommentDto> createComment(
		@PathVariable UUID feedId,
		@RequestBody @Valid CommentCreateRequest request) {
		CommentDto comment = commentService.createComment(feedId, request);
		return ResponseEntity.status(HttpStatus.CREATED).body(comment);
	}

	@GetMapping("/{feedId}/comments")
	public ResponseEntity<CommentDtoCursorResponse> getComments(
		@PathVariable UUID feedId,
		@RequestParam(required = false) Instant cursor,
		@RequestParam(required = false) UUID idAfter,
		@RequestParam int limit) {
		CommentDtoCursorResponse commentsByCursor = commentService.listByCursor(feedId, cursor,
			idAfter, limit);
		return ResponseEntity.status(HttpStatus.OK).body(commentsByCursor);
	}
}
