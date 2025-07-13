package com.codeit.otboo.domain.feed.entity;

import com.codeit.otboo.domain.clothes.entity.Clothes;
import com.codeit.otboo.global.base.BaseEntity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "ootds")
public class Ootd extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "feed_id", nullable = false)
	private Feed feed;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "clothes_id", nullable = false)
	private Clothes clothes;

	public Ootd(Feed feed, Clothes clothes) {
		this.feed = feed;
		this.clothes = clothes;
	}

	public void setNull() {
		this.feed = null;
	}
}
