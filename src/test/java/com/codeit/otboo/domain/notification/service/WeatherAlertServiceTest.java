package com.codeit.otboo.domain.notification.service;

import com.codeit.otboo.domain.notification.entity.Notification;
import com.codeit.otboo.domain.notification.repository.NotificationRepository;
import com.codeit.otboo.domain.user.entity.User;
import com.codeit.otboo.domain.user.repository.UserRepository;
import com.codeit.otboo.domain.weather.entity.Weather;
import com.codeit.otboo.domain.weather.entity.vo.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.UUID;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WeatherAlertServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private WeatherAlertService weatherAlertService;

    @Test
    @DisplayName("오늘 맑음, 내일 비 예보 시 사용자에게 알림을 생성한다")
    void generateWeatherAlerts_whenRainIsForecasted_createsNotification() {
        // given (상황 설정)
        LocationInfo location = new LocationInfo(37.0, 127.0, 60, 127);

        // User 엔티티를 직접 생성하여 테스트에 필요한 값을 설정
        User mockUser = new User();
        mockUser.setId(UUID.randomUUID());
        mockUser.setEmail("test@test.com");
        mockUser.setName("테스터");

        // 오늘 날씨: 맑음
        Weather todayWeather = Weather.builder()
                .location(location)
                .forecastAt(LocalDate.now(ZoneId.of("Asia/Seoul")).atStartOfDay(ZoneId.of("Asia/Seoul")).toInstant())
                .precipitationType(PrecipitationType.NONE)
                .temperature(new TemperatureInfo(25, 20, 30, 0))
                .build();

        // 내일 날씨: 비
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
        ArgumentCaptor<List<Notification>> captor = ArgumentCaptor.forClass(List.class);
        verify(notificationRepository, times(1)).saveAll(captor.capture());

        List<Notification> savedNotifications = captor.getValue();
        assertThat(savedNotifications).hasSize(1);
        assertThat(savedNotifications.get(0).getContent()).contains("비 또는 눈 소식");
    }
}