package com.codeit.otboo.domain.weather.entity;

import com.codeit.otboo.domain.weather.entity.vo.*;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Type;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.OffsetDateTime;
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

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @Column(nullable = false)
    private OffsetDateTime forecastedAt;

    @Column(nullable = false)
    private OffsetDateTime forecastAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "skystatus", nullable = false)
    @ColumnDefault("'CLEAR'")
    private SkyStatus skyStatus;

    @Type(JsonType.class)
    @Column(columnDefinition = "json")
    private LocationInfo location;

    @Type(JsonType.class)
    @Column(columnDefinition = "json")
    private PrecipitationInfo precipitation;

    @Type(JsonType.class)
    @Column(columnDefinition = "json")
    private HumidityInfo humidity;

    @Type(JsonType.class)
    @Column(columnDefinition = "json")
    private TemperatureInfo temperature;

    @Type(JsonType.class)
    @Column(columnDefinition = "json")
    private WindSpeedInfo windSpeed;

    @Builder
    public Weather(OffsetDateTime forecastedAt, OffsetDateTime forecastAt, SkyStatus skyStatus, LocationInfo location,
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