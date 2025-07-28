package com.codeit.otboo.domain.weather.repository;

import com.codeit.otboo.domain.weather.entity.Weather;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WeatherRepository extends JpaRepository<Weather, UUID> {

    @Query(value = "SELECT * FROM weathers w " +
            "WHERE CAST(w.location ->> 'x' AS INTEGER) = :x " +
            "AND CAST(w.location ->> 'y' AS INTEGER) = :y "
            + "ORDER BY w.forecasted_at DESC LIMIT 1",
            nativeQuery = true)
    Optional<Weather> findLatestWeatherByLocation(@Param("x") int x, @Param("y") int y);

    @Query(value = "SELECT * FROM weathers w " +
            "WHERE CAST(w.location ->> 'x' AS INTEGER) = :x " +
            "AND CAST(w.location ->> 'y' AS INTEGER) = :y " +
            "AND w.forecast_at >= :startDate " +
            "ORDER BY w.forecast_at ASC",
            nativeQuery = true)
    List<Weather> findFutureWeatherByLocation(@Param("x") int x, @Param("y") int y, @Param("startDate") Instant startDate);

    /**
     * ✨ 특정 위치와 예보 날짜로 데이터를 찾는 메서드
     */
    @Query(value = "SELECT * FROM weathers w " +
            "WHERE CAST(w.location ->> 'x' AS INTEGER) = :x " +
            "AND CAST(w.location ->> 'y' AS INTEGER) = :y " +
            "AND w.forecast_at = :forecastAt",
            nativeQuery = true)
    Optional<Weather> findByLocationAndForecastAt(@Param("x") int x, @Param("y") int y, @Param("forecastAt") Instant forecastAt);

    @Modifying
    @Transactional
    @Query("DELETE FROM Weather w WHERE w.forecastAt < :cutoffDate AND w.id NOT IN (SELECT f.weather.id FROM Feed f)")
    int deleteByForecastAtBefore(@Param("cutoffDate") Instant cutoffDate);
}