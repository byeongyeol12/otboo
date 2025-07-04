package com.codeit.otboo.domain.weather.repository;

import com.codeit.otboo.domain.weather.entity.Weather;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
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
}