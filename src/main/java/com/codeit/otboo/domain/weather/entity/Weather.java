// src/main/java/com/codeit/otboo/domain/weather/entity/Weather.java
package com.codeit.otboo.domain.weather.entity;

import com.codeit.otboo.domain.weather.entity.vo.*;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "weathers")
@EntityListeners(AuditingEntityListener.class)
public class Weather {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "forecasted_at", nullable = false)
    private Instant forecastedAt;

    @Column(name = "forecast_at", nullable = false)
    private Instant forecastAt;

    @Type(JsonType.class)
    @Column(name = "location", columnDefinition = "json")
    private LocationInfo location;

    @Enumerated(EnumType.STRING)
    @Column(name = "sky_status", nullable = false)
    private SkyStatus skyStatus;

    @Type(JsonType.class)
    @Column(name = "precipitation", columnDefinition = "json")
    private PrecipitationInfo precipitation;

    @Type(JsonType.class)
    @Column(name = "humidity", columnDefinition = "json")
    private HumidityInfo humidity;

    @Type(JsonType.class)
    @Column(name = "temperature", columnDefinition = "json")
    private TemperatureInfo temperature;

    @Type(JsonType.class)
    @Column(name = "wind_speed", columnDefinition = "json")
    private WindSpeedInfo windSpeed;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;

    @Builder
    public Weather(Instant forecastedAt, Instant forecastAt, SkyStatus skyStatus, LocationInfo location,
                   PrecipitationInfo precipitation, HumidityInfo humidity, TemperatureInfo temperature, WindSpeedInfo windSpeed) {
        this.forecastedAt = forecastedAt;
        this.forecastAt = forecastAt;
        this.skyStatus = skyStatus;
        this.location = location;
        this.precipitation = precipitation;
        this.humidity = humidity;
        this.temperature = temperature;
        this.windSpeed = windSpeed;
    }
}