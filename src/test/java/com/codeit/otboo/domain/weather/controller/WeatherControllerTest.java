package com.codeit.otboo.domain.weather.controller;

import com.codeit.otboo.domain.weather.dto.WeatherDto;
import com.codeit.otboo.domain.weather.service.WeatherService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WeatherController.class) // WeatherController와 관련된 Bean만 로드하여 가볍게 테스트
class WeatherControllerTest {

    @Autowired
    private MockMvc mockMvc; // API 호출을 시뮬레이션하기 위한 객체

    @MockBean // WeatherController가 의존하는 WeatherService는 가짜(Mock) 객체로 대체
    private WeatherService weatherService;

    @Test
    @WithMockUser
    @DisplayName("GET /api/weathers 요청 시, 날씨 DTO 목록과 함께 200 OK를 반환한다")
    void getWeathers_returnsWeatherList_withOk() throws Exception {
        // given: weatherService가 호출되면, 비어있는 리스트를 반환하도록 설정
        when(weatherService.getWeather(anyDouble(), anyDouble()))
                .thenReturn(Collections.emptyList());

        // when & then: /api/weathers GET 요청을 보내고 응답을 검증
        mockMvc.perform(get("/api/weathers")
                        .param("latitude", "37.0")
                        .param("longitude", "127.0")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()) // HTTP 상태가 200 OK인지 확인
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray()); // 응답 본문이 JSON 배열인지 확인
    }
}