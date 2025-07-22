package com.codeit.otboo.domain.notification.service;

import com.codeit.otboo.domain.notification.dto.NotificationDto;
import com.codeit.otboo.domain.user.entity.User;
import com.codeit.otboo.domain.user.repository.UserRepository;
import com.codeit.otboo.domain.weather.entity.Weather;
import com.codeit.otboo.domain.weather.entity.vo.LocationInfo;
import com.codeit.otboo.domain.weather.entity.vo.PrecipitationType;
import com.codeit.otboo.domain.weather.entity.vo.TemperatureInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WeatherAlertServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationService notificationService; // ✨ Mock 대상을 NotificationRepository -> NotificationService로 변경

    @InjectMocks
    private WeatherAlertService weatherAlertService;

    @Test
    @DisplayName("오늘 맑음, 내일 비 예보 시 NotificationService를 호출하여 알림을 요청한다")
    void generateWeatherAlerts_whenRainIsForecasted_requestsNotificationCreation() {
        // given (상황 설정)
        LocationInfo location = new LocationInfo(37.0, 127.0, 60, 127);

        User mockUser = new User();
        mockUser.setId(UUID.randomUUID());

        Weather todayWeather = Weather.builder()
                .location(location)
                .forecastAt(LocalDate.now(ZoneId.of("Asia/Seoul")).atStartOfDay(ZoneId.of("Asia/Seoul")).toInstant())
                .precipitationType(PrecipitationType.NONE)
                .temperature(new TemperatureInfo(25, 20, 30, 0))
                .build();

        Weather tomorrowWeather = Weather.builder()
                .location(location)
                .forecastAt(LocalDate.now(ZoneId.of("Asia/Seoul")).plusDays(1).atStartOfDay(ZoneId.of("Asia/Seoul")).toInstant())
                .precipitationType(PrecipitationType.RAIN)
                .temperature(new TemperatureInfo(22, 18, 25, -5))
                .build();

        List<Weather> weathers = List.of(todayWeather, tomorrowWeather);

        when(userRepository.findByProfileXAndProfileY(60, 127)).thenReturn(List.of(mockUser));

        // when (기능 실행)
        weatherAlertService.generateWeatherAlerts(weathers);

        // then (결과 검증)
        // ✨ notificationService.createAndSend()가 호출되었는지, 어떤 DTO가 전달되었는지 검증
        ArgumentCaptor<NotificationDto> captor = ArgumentCaptor.forClass(NotificationDto.class);
        verify(notificationService, times(1)).createAndSend(captor.capture());

        NotificationDto createdDto = captor.getValue();
        assertThat(createdDto.receiverId()).isEqualTo(mockUser.getId());
        assertThat(createdDto.content()).contains("비 또는 눈 소식");
    }
}