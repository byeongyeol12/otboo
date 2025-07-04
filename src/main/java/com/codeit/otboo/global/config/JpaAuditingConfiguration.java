package com.codeit.otboo.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.time.OffsetDateTime;
import java.util.Optional;

@Configuration
@EnableJpaAuditing(dateTimeProviderRef = "offsetDateTimeProvider")
public class JpaAuditingConfiguration {

    /**
     * JPA Auditing 기능이 @CreatedDate, @LastModifiedDate에 값을 채울 때 사용할
     * 시간 정보 제공자(Provider)를 정의합니다.
     * @return OffsetDateTime 타입의 현재 시간을 반환하는 DateTimeProvider
     */
    @Bean(name = "offsetDateTimeProvider")
    public DateTimeProvider dateTimeProvider() {
        return () -> Optional.of(OffsetDateTime.now());
    }
}
