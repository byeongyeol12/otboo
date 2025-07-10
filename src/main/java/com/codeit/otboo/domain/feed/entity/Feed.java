package com.codeit.otboo.domain.feed.entity;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.codeit.otboo.domain.clothes.entity.Clothes;
import com.codeit.otboo.domain.user.entity.User;
import com.codeit.otboo.domain.weather.entity.Weather;
import com.codeit.otboo.global.base.BaseUpdatableEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "feeds")
public class Feed extends BaseUpdatableEntity {
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "weather_id", nullable = false)
	private Weather weather;

	@Column(name = "liked_by_me", nullable = false)
	private boolean likedByMe;

	@Column(name = "like_count", nullable = false)
	private Long likeCount;

	@Column(name = "comment_count", nullable = false)
	private Integer commentCount;

	@Column(name = "content")
	private String content;

	@OneToMany(mappedBy = "feed",
		cascade = CascadeType.ALL,
		orphanRemoval = true)
	private List<Ootd> clothesFeeds = new ArrayList<>();

	@OneToMany(mappedBy = "feed",
		cascade = CascadeType.ALL,
		orphanRemoval = true)
	private List<FeedComment> feedComments = new ArrayList<>();

	@Builder
	public Feed(User user, Weather weather, boolean likedByMe, Long likeCount, Integer commentCount, String content) {
		this.user = user;
		this.weather = weather;
		this.likedByMe = likedByMe;
		this.likeCount = likeCount;
		this.commentCount = commentCount;
		this.content = content;
	}

	public void addClothes(Clothes clothes) {
		Ootd link = new Ootd(this, clothes);
		this.clothesFeeds.add(link);
	} // using *ToMany logic, should care entity field in business logic

	public void removeClothesFeed(Ootd cf) {
		this.clothesFeeds.remove(cf); //remove entity field
		cf.setNull(); // set up feed field to null. using orphan removal, get rid of middle table(Ootds)
	}



	public void updateContent(String newContent) {
		if (newContent != null && !newContent.equals(this.content)) {
			this.content = newContent;
		}
	}

	public void addComment() {
		this.commentCount++;
	}

	public void removeComment() {
		this.commentCount--;
	}

	public void like() {
		this.likeCount++;
	}

	public void likedByMe() {
		this.likedByMe = true;
	}

	public void unlike() {
		this.likeCount--;
	}

	public void unlikedByMe() {
		this.likedByMe = false;
	}

}
