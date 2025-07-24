package com.codeit.otboo.domain.weather.repository;

import com.codeit.otboo.domain.weather.entity.Weather;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WeatherRepository extends JpaRepository<Weather, UUID> {

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

    // 피드/추천 등에서 만료된 ID 처리용 메서드
    @Query(value = "SELECT * FROM weathers w " +
            "WHERE CAST(w.location ->> 'x' AS INTEGER) = :x " +
            "AND CAST(w.location ->> 'y' AS INTEGER) = :y "
            + "ORDER BY w.forecasted_at DESC LIMIT 1",
            nativeQuery = true)
    Optional<Weather> findLatestWeatherByLocation(@Param("x") int x, @Param("y") int y);

    /**
     * 특정 위치의 모든 일별 예보를 날짜순으로 조회합니다.
     * JPQL 대신 Native Query를 사용하여 'not joinable' 에러를 해결합니다.
     */
    @Query(value = "SELECT * FROM weathers w " +
            "WHERE CAST(w.location ->> 'x' AS INTEGER) = :x " +
            "AND CAST(w.location ->> 'y' AS INTEGER) = :y " +
            "ORDER BY w.forecast_at ASC",
            nativeQuery = true)
    List<Weather> findByLocationXAndLocationYOrderByForecastAtAsc(@Param("x") int x, @Param("y") int y);
}