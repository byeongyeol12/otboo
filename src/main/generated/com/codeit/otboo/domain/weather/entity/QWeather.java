package com.codeit.otboo.domain.weather.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QWeather is a Querydsl query type for Weather
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QWeather extends EntityPathBase<Weather> {

    private static final long serialVersionUID = 138838387L;

    public static final QWeather weather = new QWeather("weather");

    public final DateTimePath<java.time.Instant> createdAt = createDateTime("createdAt", java.time.Instant.class);

    public final DateTimePath<java.time.Instant> forecastAt = createDateTime("forecastAt", java.time.Instant.class);

    public final DateTimePath<java.time.Instant> forecastedAt = createDateTime("forecastedAt", java.time.Instant.class);

    public final SimplePath<com.codeit.otboo.domain.weather.entity.vo.HumidityInfo> humidity = createSimple("humidity", com.codeit.otboo.domain.weather.entity.vo.HumidityInfo.class);

    public final ComparablePath<java.util.UUID> id = createComparable("id", java.util.UUID.class);

    public final SimplePath<com.codeit.otboo.domain.weather.entity.vo.LocationInfo> location = createSimple("location", com.codeit.otboo.domain.weather.entity.vo.LocationInfo.class);

    public final SimplePath<com.codeit.otboo.domain.weather.entity.vo.PrecipitationInfo> precipitation = createSimple("precipitation", com.codeit.otboo.domain.weather.entity.vo.PrecipitationInfo.class);

    public final EnumPath<com.codeit.otboo.domain.weather.entity.vo.SkyStatus> skyStatus = createEnum("skyStatus", com.codeit.otboo.domain.weather.entity.vo.SkyStatus.class);

    public final SimplePath<com.codeit.otboo.domain.weather.entity.vo.TemperatureInfo> temperature = createSimple("temperature", com.codeit.otboo.domain.weather.entity.vo.TemperatureInfo.class);

    public final DateTimePath<java.time.Instant> updatedAt = createDateTime("updatedAt", java.time.Instant.class);

    public final SimplePath<com.codeit.otboo.domain.weather.entity.vo.WindSpeedInfo> windSpeed = createSimple("windSpeed", com.codeit.otboo.domain.weather.entity.vo.WindSpeedInfo.class);

    public QWeather(String variable) {
        super(Weather.class, forVariable(variable));
    }

    public QWeather(Path<? extends Weather> path) {
        super(path.getType(), path.getMetadata());
    }

    public QWeather(PathMetadata metadata) {
        super(Weather.class, metadata);
    }

}

