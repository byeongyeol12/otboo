package com.codeit.otboo.domain.user.mapper;

import com.codeit.otboo.domain.user.dto.request.ProfileUpdateRequest;
import com.codeit.otboo.domain.user.dto.request.UserCreateRequest;
import com.codeit.otboo.domain.user.dto.response.ProfileDto;
import com.codeit.otboo.domain.user.entity.Profile;
import com.codeit.otboo.domain.user.entity.User;
import com.codeit.otboo.global.enumType.Gender;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-07-07T10:10:11+0900",
    comments = "version: 1.5.3.Final, compiler: IncrementalProcessingEnvironment from gradle-language-java-8.14.2.jar, environment: Java 17.0.15 (Homebrew)"
)
@Component
public class ProfileMapperImpl implements ProfileMapper {

    @Override
    public Profile toEntity(UserCreateRequest request) {
        if ( request == null ) {
            return null;
        }

        Profile profile = new Profile();

        return profile;
    }

    @Override
    public ProfileDto toDto(Profile profile) {
        if ( profile == null ) {
            return null;
        }

        UUID userId = null;
        String name = null;
        Object location = null;
        Gender gender = null;
        String birthDate = null;
        Integer temperatureSensitivity = null;
        String profileImageUrl = null;

        userId = profileUserId( profile );
        name = profileUserName( profile );
        location = profile.getLocationNames();
        gender = profile.getGender();
        if ( profile.getBirthDate() != null ) {
            birthDate = profile.getBirthDate().toString();
        }
        temperatureSensitivity = profile.getTemperatureSensitivity();
        profileImageUrl = profile.getProfileImageUrl();

        ProfileDto profileDto = new ProfileDto( userId, name, gender, birthDate, location, temperatureSensitivity, profileImageUrl );

        return profileDto;
    }

    @Override
    public void updateProfileFromRequest(ProfileUpdateRequest request, Profile profile) {
        if ( request == null ) {
            return;
        }
    }

    private UUID profileUserId(Profile profile) {
        if ( profile == null ) {
            return null;
        }
        User user = profile.getUser();
        if ( user == null ) {
            return null;
        }
        UUID id = user.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private String profileUserName(Profile profile) {
        if ( profile == null ) {
            return null;
        }
        User user = profile.getUser();
        if ( user == null ) {
            return null;
        }
        String name = user.getName();
        if ( name == null ) {
            return null;
        }
        return name;
    }
}
