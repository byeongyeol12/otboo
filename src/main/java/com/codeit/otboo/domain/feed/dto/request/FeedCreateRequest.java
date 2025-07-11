package com.codeit.otboo.domain.feed.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FeedCreateRequest {

	@NotNull(message = "작성자 ID는 필수입니다")
	private UUID authorId;
	@NotNull(message = "날씨 ID는 필수입니다")
	private UUID weatherId;

	private List<UUID> clothesIds;
	@NotNull(message = "내용을 입력해주세요")
	private String content;

}
