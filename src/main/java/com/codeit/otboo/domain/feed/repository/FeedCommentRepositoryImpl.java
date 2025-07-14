package com.codeit.otboo.domain.feed.repository;

import static com.codeit.otboo.domain.feed.entity.QFeedComment.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.codeit.otboo.domain.feed.entity.FeedComment;
import com.codeit.otboo.domain.user.entity.QUser;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;

import jakarta.persistence.EntityManager;

@Repository
public class FeedCommentRepositoryImpl implements FeedCommentRepositoryCustom {

	private final JPAQueryFactory queryFactory;
	private final QUser author = QUser.user;

	public FeedCommentRepositoryImpl(EntityManager em) {
		this.queryFactory = new JPAQueryFactory(em);
	}

	@Override
	public List<FeedComment> findByFeedIdCursor(
		UUID feedId,
		Instant cursor,
		UUID idAfter,
		int limit
	) {
		BooleanBuilder where = new BooleanBuilder();
		where.and(feedComment.feed.id.eq(feedId));

		if (cursor != null) {
			// (c.createdAt < cursor) OR (c.createdAt = cursor AND c.id < idAfter)
			where.and(
				feedComment.createdAt.lt(cursor)
					.or(feedComment.createdAt.eq(cursor)
						.and(feedComment.id.lt(idAfter)))
			);
		}

		return queryFactory
			.selectFrom(feedComment)
			.join(feedComment.author, author).fetchJoin()
			.where(where)
			.orderBy(feedComment.createdAt.desc(), feedComment.id.desc())
			.limit(limit)
			.fetch();
	}
}
