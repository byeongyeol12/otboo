package com.codeit.otboo.domain.weather.repository;

import com.codeit.otboo.domain.weather.entity.Weather;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional; // Optional 임포트 추가
import java.util.UUID;

@Repository
public interface WeatherRepository extends JpaRepository<Weather, UUID> {

    /**
     * JSON으로 저장된 location의 x, y 값을 조건으로 날씨 정보를 조회합니다.
     * 데이터베이스의 네이티브 JSON 쿼리 기능을 사용합니다. (PostgreSQL 기준)
     */
    @Query(value = "SELECT w.* FROM weathers w " +
            "WHERE CAST(w.location ->> 'x' AS INTEGER) = :x " +
            "AND CAST(w.location ->> 'y' AS INTEGER) = :y " +
            "AND w.forecast_at > :forecastAtAfter " +
            "ORDER BY w.forecasted_at DESC, w.forecast_at ASC",
            nativeQuery = true)
    List<Weather> findWeathersByLocation(
            @Param("x") int x,
            @Param("y") int y,
            @Param("forecastAtAfter") OffsetDateTime forecastAtAfter
    );

    // <<< 이 메서드를 여기에 추가하세요.
    /**
     * 특정 위치에서 현재 시간 이후의 가장 최신/가까운 예보 1개를 조회합니다.
     */
    @Query(value = "SELECT w.* FROM weathers w " +
            "WHERE CAST(w.location ->> 'x' AS INTEGER) = :x " +
            "AND CAST(w.location ->> 'y' AS INTEGER) = :y " +
            "AND w.forecast_at > :now " +
            "ORDER BY w.forecast_at ASC " +
            "LIMIT 1",
            nativeQuery = true)
    Optional<Weather> findLatestForecastByLocation(
            @Param("x") int x,
            @Param("y") int y,
            @Param("now") OffsetDateTime now
    );
}