// src/main/java/com/codeit/otboo/domain/weather/scheduler/WeatherDataCleanupService.java
package com.codeit.otboo.domain.weather.scheduler;

import com.codeit.otboo.domain.weather.repository.WeatherRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeatherDataCleanupService {

    private final WeatherRepository weatherRepository;

    public void cleanupOldWeatherData() {
        log.info("Starting scheduled job: Deleting old weather data...");

        // 현재 시간으로부터 2일 전을 기준 시각으로 설정
        Instant cutoffDate = Instant.now().minus(2, ChronoUnit.DAYS);

        // 기준 시각 이전의 모든 날씨 데이터 삭제
        int deletedCount = weatherRepository.deleteByForecastAtBefore(cutoffDate);

        log.info("Finished scheduled job: Deleted {} old weather records.", deletedCount);
    }
}