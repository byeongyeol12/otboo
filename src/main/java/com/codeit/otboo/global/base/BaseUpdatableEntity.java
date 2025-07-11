package com.codeit.otboo.global.base;

import java.time.Instant;

import org.springframework.data.annotation.LastModifiedDate;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@MappedSuperclass
@Getter
public class BaseUpdatableEntity extends BaseEntity {
	@LastModifiedDate
	@Column(name = "updated_at")
	private Instant updatedAt;
}
