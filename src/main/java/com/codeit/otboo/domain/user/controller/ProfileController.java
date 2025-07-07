package com.codeit.otboo.domain.user.controller;

import com.codeit.otboo.domain.user.dto.request.ProfileLocationUpdateRequest;
import com.codeit.otboo.domain.user.dto.response.ProfileResponseDto;
import com.codeit.otboo.domain.user.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users/{userId}/profiles")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    // 프로필의 '위치 정보'만 수정하는 API
    @PatchMapping("/location")
    public ResponseEntity<ProfileResponseDto> updateProfileLocation(
            @PathVariable UUID userId,
            @RequestBody ProfileLocationUpdateRequest request
    ) {
        ProfileResponseDto updatedProfile = profileService.updateLocation(userId, request);
        return ResponseEntity.ok(updatedProfile);
    }
}