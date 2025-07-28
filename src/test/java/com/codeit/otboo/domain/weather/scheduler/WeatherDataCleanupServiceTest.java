package com.codeit.otboo.domain.weather.scheduler;

import com.codeit.otboo.domain.weather.repository.WeatherRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WeatherDataCleanupServiceTest {

    @Mock
    private WeatherRepository weatherRepository;

    @InjectMocks
    private WeatherDataCleanupService weatherDataCleanupService;

    @Test
    @DisplayName("cleanupOldWeatherData는 2일 전을 기준으로 repository의 삭제 메서드를 호출한다")
    void cleanupOldWeatherData_callsRepository_withCorrectCutoffDate() {
        // given: repository의 delete 메서드가 호출될 때, 10을 반환하도록 설정
        when(weatherRepository.deleteByForecastAtBefore(any(Instant.class))).thenReturn(10);

        // when: 서비스의 cleanup 메서드를 실행하면
        weatherDataCleanupService.cleanupOldWeatherData();

        // then: repository의 deleteByForecastAtBefore 메서드가 1번 호출되었는지 검증

        // 1. ArgumentCaptor를 사용하여 메서드에 전달된 실제 Instant 값을 캡처
        ArgumentCaptor<Instant> instantCaptor = ArgumentCaptor.forClass(Instant.class);
        verify(weatherRepository, times(1)).deleteByForecastAtBefore(instantCaptor.capture());

        // 2. 캡처된 값이 '약 2일 전'인지 확인
        Instant capturedCutoffDate = instantCaptor.getValue();
        Instant expectedCutoffDate = Instant.now().minus(2, ChronoUnit.DAYS);

        // 테스트 실행 시점의 미세한 시간 차이를 감안하여, 1초 이내인지 검증
        Duration durationBetween = Duration.between(capturedCutoffDate, expectedCutoffDate);
        assertThat(durationBetween.toSeconds()).isLessThan(1);
    }
}