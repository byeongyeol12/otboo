package com.codeit.otboo.domain.user.mapper;

import static org.assertj.core.api.Assertions.*;

import java.lang.reflect.Field;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import com.codeit.otboo.domain.user.dto.request.UserCreateRequest;
import com.codeit.otboo.domain.user.dto.response.LocationDto;
import com.codeit.otboo.domain.user.dto.response.ProfileDto;
import com.codeit.otboo.domain.user.dto.response.UserDto;
import com.codeit.otboo.domain.user.entity.Profile;
import com.codeit.otboo.domain.user.entity.User;
import com.codeit.otboo.global.enumType.Gender;
import com.codeit.otboo.global.enumType.Role;

public class UserMapperTest {

	private final UserMapper userMapper = Mappers.getMapper(UserMapper.class);
	private final ProfileMapper profileMapper = Mappers.getMapper(ProfileMapper.class);

	@Test
	@DisplayName("UserCreateRequest -> User 변환")
	void userCreateRequest_to_user() {
		UserCreateRequest request = new UserCreateRequest("테스트", "test@example.com", "Test1234!");
		User user = userMapper.toEntity(request);

		assertThat(user.getEmail()).isEqualTo("test@example.com");
		assertThat(user.getName()).isEqualTo("테스트");
		assertThat(user.getPasswordHash()).isNull();
		assertThat(user.getRole()).isNull();
	}

	@Test
	@DisplayName("User -> UserDto 변환")
	void user_to_userDto() {
		User user = new User();
		UUID id = UUID.randomUUID();
		setField(user, "id", id);
		setField(user, "email", "dto@ootd.com");
		setField(user, "name", "DTO");
		setField(user, "role", Role.USER);
		setField(user, "locked", false);
		setField(user, "createdAt", Instant.parse("2023-01-01T00:00:00Z"));

		UserDto dto = userMapper.toDto(user);
		assertThat(dto.id()).isEqualTo(id);
		assertThat(dto.email()).isEqualTo("dto@ootd.com");
		assertThat(dto.name()).isEqualTo("DTO");
		assertThat(dto.role()).isEqualTo(Role.USER);
		assertThat(dto.locked()).isFalse();
	}

	@Test
	@DisplayName("User 리스트 -> UserDto 리스트 변환")
	void userList_to_userDtoList() {
		User u1 = new User();
		setField(u1, "id", UUID.randomUUID());
		setField(u1, "email", "a@ootd.com");
		setField(u1, "name", "A");

		User u2 = new User();
		setField(u2, "id", UUID.randomUUID());
		setField(u2, "email", "b@ootd.com");
		setField(u2, "name", "B");

		List<UserDto> result = userMapper.toDtoList(List.of(u1, u2));
		assertThat(result).hasSize(2);
		assertThat(result).extracting(UserDto::name).containsExactly("A", "B");
	}

	@Test
	@DisplayName("Profile -> ProfileDto 변환")
	void profile_to_profileDto() {
		User user = new User();
		UUID userId = UUID.randomUUID();
		setField(user, "id", userId);
		setField(user, "name", "홍길동");

		Profile profile = new Profile(user, "닉네임", Gender.MALE);
		profile.updateLocation(37.5, 127.0, 60, 127, "서울특별시, 강남구");
		Instant birth = Instant.parse("2000-01-01T00:00:00Z");
		profile.updateProfile("닉네임", Gender.MALE, birth, "서울특별시, 강남구", 4, "http://image.com");

		ProfileDto dto = profileMapper.toDto(profile);

		assertThat(dto.userId()).isEqualTo(userId);
		assertThat(dto.name()).isEqualTo("홍길동");
		assertThat(dto.gender()).isEqualTo(Gender.MALE);
		assertThat(dto.birthDate()).isEqualTo(LocalDate.of(2000, 1, 1));
		assertThat(dto.temperatureSensitivity()).isEqualTo(4);
		assertThat(dto.profileImageUrl()).isEqualTo("http://image.com");

		LocationDto location = dto.location();
		assertThat(location.latitude()).isEqualTo(37.5);
		assertThat(location.longitude()).isEqualTo(127.0);
		assertThat(location.locationNames()).contains("서울특별시", "강남구");
	}

	@Test
	@DisplayName("Instant -> LocalDate 변환")
	void instant_to_localDate() {
		Instant now = Instant.parse("2000-01-01T00:00:00Z");
		assertThat(profileMapper.map(now)).isEqualTo(LocalDate.of(2000, 1, 1));
	}

	private void setField(Object target, String fieldName, Object value) {
		try {
			Field field = target.getClass().getDeclaredField(fieldName);
			field.setAccessible(true);
			field.set(target, value);
		} catch (Exception e) {
			throw new RuntimeException("필드 설정 실패: " + fieldName, e);
		}
	}
}
