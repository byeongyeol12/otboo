package com.codeit.otboo.domain.location.controller;

import com.codeit.otboo.domain.location.dto.response.LocationResponse;
import com.codeit.otboo.domain.location.service.LocationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LocationController.class)
class LocationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LocationService locationService;

    @Test
    @WithMockUser // Spring Security 인증을 통과하기 위해 가짜 사용자로 테스트 실행
    @DisplayName("GET /api/weathers/location 요청 시, 위치 정보 DTO와 함께 200 OK를 반환한다")
    void getLocation_returnsLocationResponse_withOk() throws Exception {
        // given: locationService가 호출되면, 미리 정의된 가짜 응답을 반환하도록 설정
        double lat = 37.5665;
        double lon = 126.9780;
        LocationResponse fakeResponse = new LocationResponse(
                lat, lon, 60, 127, List.of("경기도", "양주시", "회천4동")
        );
        when(locationService.getLocationInfo(anyDouble(), anyDouble())).thenReturn(fakeResponse);

        // when & then: API를 호출하고 응답을 검증
        mockMvc.perform(get("/api/weathers/location")
                        .param("latitude", String.valueOf(lat))
                        .param("longitude", String.valueOf(lon))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()) // HTTP 상태가 200 OK인지 확인
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.x").value(60)) // JSON 응답 내용 검증
                .andExpect(jsonPath("$.y").value(127))
                .andExpect(jsonPath("$.locationNames[0]").value("경기도"));
    }
}