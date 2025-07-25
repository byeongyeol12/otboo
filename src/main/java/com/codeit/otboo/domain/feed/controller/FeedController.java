package com.codeit.otboo.domain.feed.controller;

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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.UUID;

@Tag(name = "피드", description = "피드, 댓글, 좋아요 관련 API")
@RestController
@RequestMapping("/api/feeds")
@RequiredArgsConstructor
public class FeedController {
	private final FeedService feedService;
	private final FeedLikeService feedLikeService;
	private final CommentService commentService;


	@Operation(summary = "피드 생성", description = "새로운 OOTD 피드를 등록합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "201", description = "피드 생성 성공"),
			@ApiResponse(responseCode = "404", description = "필요한 정보(사용자, 날씨, 옷 등)를 찾을 수 없음")
	})
	@PostMapping
	public ResponseEntity<FeedDto> createFeed(@RequestBody @Valid FeedCreateRequest request) {
		FeedDto feed = feedService.createFeed(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(feed);
	}

	@Operation(summary = "피드 목록 조회", description = "다양한 조건으로 피드 목록을 조회합니다. 커서 기반 페이지네이션을 사용합니다.")
	@ApiResponse(responseCode = "200", description = "조회 성공")
	@GetMapping
	public ResponseEntity<FeedDtoCursorResponse> getFeeds(
			@Parameter(description = "페이지네이션 커서 (정렬 기준이 createdAt일 경우 ISO 8601 형식, likeCount일 경우 좋아요 수)") @RequestParam(name = "cursor", required = false) String cursor,
			@Parameter(description = "기준 ID") @RequestParam(name = "idAfter", required = false) UUID idAfter,
			@Parameter(description = "조회할 개수") @RequestParam(name = "limit", defaultValue = "20") int limit,
			@Parameter(description = "정렬 기준 (createdAt 또는 likeCount)") @RequestParam(name = "sortBy", defaultValue = "createdAt") String sortBy,
			@Parameter(description = "정렬 방향 (ASC 또는 DESC)") @RequestParam(name = "sortDirection", defaultValue = "DESCENDING") String sortDirection,
			@Parameter(description = "내용 검색 키워드") @RequestParam(name = "keywordLike", required = false) String keywordLike,
			@Parameter(description = "날씨 필터 (하늘 상태)") @RequestParam(name = "skyStatusEqual", required = false) SkyStatus skyStatusEqual,
			@Parameter(description = "날씨 필터 (강수 형태)") @RequestParam(name = "precipitationTypeEqual", required = false) PrecipitationType precipitationTypeEqual,
			@Parameter(description = "작성자 ID 필터") @RequestParam(name = "authorIdEqual", required = false) UUID authorIdEqual) {
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

	@Operation(summary = "피드 수정", description = "자신이 작성한 피드의 내용을 수정합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "수정 성공"),
			@ApiResponse(responseCode = "403", description = "자신의 피드만 수정할 수 있음"),
			@ApiResponse(responseCode = "404", description = "해당 피드를 찾을 수 없음")
	})
	@PatchMapping("/{feedId}")
	public ResponseEntity<FeedDto> updateFeed(
			@Parameter(description = "수정할 피드의 ID") @PathVariable UUID feedId,
			@RequestBody @Valid FeedUpdateRequest request) {
		FeedDto feed = feedService.updateFeed(feedId, request);
		return ResponseEntity.status(HttpStatus.OK).body(feed);
	}

	@Operation(summary = "피드 삭제", description = "자신이 작성한 피드를 삭제합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "204", description = "삭제 성공"),
			@ApiResponse(responseCode = "403", description = "자신의 피드만 삭제할 수 있음"),
			@ApiResponse(responseCode = "404", description = "해당 피드를 찾을 수 없음")
	})
	@DeleteMapping("/{feedId}")
	public ResponseEntity<Void> deleteFeed(@Parameter(description = "삭제할 피드의 ID") @PathVariable UUID feedId) {
		feedService.deleteFeed(feedId);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}

	@Operation(summary = "피드 좋아요", description = "특정 피드에 '좋아요'를 누릅니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "좋아요 성공"),
			@ApiResponse(responseCode = "404", description = "해당 피드를 찾을 수 없음")
	})
	@PostMapping("/{feedId}/like")
	public ResponseEntity<FeedDto> likeFeed(
			@Parameter(description = "좋아요를 누를 피드의 ID") @PathVariable UUID feedId,
			@Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal userPrincipal
	) {
		UUID myUserId = userPrincipal.getId();
		feedLikeService.likeFeed(myUserId, feedId);
		return ResponseEntity.status(HttpStatus.OK).body(feedService.getFeed(feedId));
	}

	@Operation(summary = "피드 좋아요 취소", description = "피드에 눌렀던 '좋아요'를 취소합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "204", description = "좋아요 취소 성공"),
			@ApiResponse(responseCode = "404", description = "해당 피드 또는 좋아요를 찾을 수 없음")
	})
	@DeleteMapping("/{feedId}/like")
	public ResponseEntity<Void> unlikeFeed(
			@Parameter(description = "좋아요를 취소할 피드의 ID") @PathVariable UUID feedId,
			@Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal userPrincipal
	) {
		UUID myUserId = userPrincipal.getId();
		feedLikeService.unlikeFeed(myUserId, feedId);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}

	@Operation(summary = "댓글 작성", description = "특정 피드에 댓글을 작성합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "201", description = "댓글 작성 성공"),
			@ApiResponse(responseCode = "404", description = "해당 피드를 찾을 수 없음")
	})
	@PostMapping("/{feedId}/comments")
	public ResponseEntity<CommentDto> createComment(
			@Parameter(description = "댓글을 작성할 피드의 ID") @PathVariable UUID feedId,
			@RequestBody @Valid CommentCreateRequest request) {
		CommentDto comment = commentService.createComment(feedId, request);
		return ResponseEntity.status(HttpStatus.CREATED).body(comment);
	}

	@Operation(summary = "댓글 목록 조회", description = "특정 피드의 댓글 목록을 조회합니다.")
	@ApiResponse(responseCode = "200", description = "조회 성공")
	@GetMapping("/{feedId}/comments")
	public ResponseEntity<CommentDtoCursorResponse> getComments(
			@Parameter(description = "댓글을 조회할 피드의 ID") @PathVariable UUID feedId,
			@Parameter(description = "페이지네이션 커서 (ISO 8601 형식)") @RequestParam(required = false) Instant cursor,
			@Parameter(description = "기준 ID") @RequestParam(required = false) UUID idAfter,
			@Parameter(description = "조회할 개수") @RequestParam int limit) {
		CommentDtoCursorResponse commentsByCursor = commentService.listByCursor(feedId, cursor,
				idAfter, limit);
		return ResponseEntity.status(HttpStatus.OK).body(commentsByCursor);
	}
}