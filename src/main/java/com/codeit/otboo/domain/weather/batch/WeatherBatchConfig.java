package com.codeit.otboo.domain.weather.batch;

import com.codeit.otboo.domain.user.entity.Profile;
import com.codeit.otboo.domain.user.repository.ProfileRepository;
import com.codeit.otboo.domain.weather.component.KmaApiClient;
import com.codeit.otboo.domain.weather.component.WeatherParser;
import com.codeit.otboo.domain.weather.dto.WeatherDto;
import com.codeit.otboo.domain.weather.entity.Weather;
import com.codeit.otboo.domain.weather.repository.WeatherRepository;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class WeatherBatchConfig {

    private final KmaApiClient kmaApiClient;
    private final WeatherParser weatherParser;
    private final WeatherRepository weatherRepository;
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final JobLauncher jobLauncher;
    private final EntityManagerFactory entityManagerFactory; // JpaPagingItemReader를 위해 주입
    private final ProfileRepository profileRepository; // 사용자 위치를 읽기 위해 주입

    @Bean
    public Job fetchWeatherJob(JobRepository jobRepository, Step fetchWeatherStep) {
        return new JobBuilder("fetchWeatherJob", jobRepository)
                .start(fetchWeatherStep)
                .build();
    }

    @Bean
    public Step fetchWeatherStep(JobRepository jobRepository, PlatformTransactionManager transactionManager,
                                 JpaPagingItemReader<Profile> profileItemReader,
                                 ItemProcessor<Profile, List<Weather>> weatherItemProcessor,
                                 ItemWriter<List<Weather>> weatherListItemWriter) {
        return new StepBuilder("fetchWeatherStep", jobRepository)
                .<Profile, List<Weather>>chunk(10, transactionManager) // 읽어올 타입이 Profile로 변경
                .reader(profileItemReader) // ItemReader 교체
                .processor(weatherItemProcessor)
                .writer(weatherListItemWriter)
                .build();
    }

    /**
     * ItemReader 구현: DB의 profiles 테이블에서 모든 프로필 정보를 페이징하여 읽어옵니다.
     */
    @Bean
    public JpaPagingItemReader<Profile> profileItemReader() {
        // 위치 정보가 등록된 프로필만 조회합니다.
        String queryString = "SELECT p FROM Profile p WHERE p.latitude IS NOT NULL AND p.longitude IS NOT NULL";

        return new JpaPagingItemReaderBuilder<Profile>()
                .name("profileItemReader")
                .entityManagerFactory(entityManagerFactory)
                .pageSize(100) // 한 번에 100개의 프로필씩 읽어옴
                .queryString(queryString)
                .build();
    }

    /**
     * ItemProcessor 구현: Profile 엔티티를 받아 API를 호출하고, Weather Entity 리스트로 변환합니다.
     */
    @Bean
    public ItemProcessor<Profile, List<Weather>> weatherItemProcessor() {
        return profile -> {
            log.info("Processing weather for user profile: {}", profile.getId());
            String rawData = kmaApiClient.fetchWeatherForecast(profile.getX(), profile.getY());
            List<WeatherDto> dtoList = weatherParser.parseAndGroup(rawData, profile.getLatitude(), profile.getLongitude());

            return dtoList.stream()
                    .map(dto -> Weather.builder()
                            .forecastedAt(dto.forecastedAt())
                            .forecastAt(dto.forecastAt())
                            .location(dto.location())
                            .temperature(dto.temperature())
                            .skyStatus(dto.skyStatus())
                            .precipitation(dto.precipitation())
                            .humidity(dto.humidity())
                            .windSpeed(dto.windSpeed())
                            .build())
                    .collect(Collectors.toList());
        };
    }

    /**
     * ItemWriter 구현: 처리된 Weather Entity 리스트를 DB에 저장합니다.
     */
    @Bean
    public ItemWriter<List<Weather>> weatherListItemWriter() {
        return chunk -> {
            List<Weather> weathersToSave = chunk.getItems().stream()
                    .flatMap(List::stream)
                    .collect(Collectors.toList());

            if (!weathersToSave.isEmpty()) {
                log.info("Saving {} weather entities to the database.", weathersToSave.size());
                weatherRepository.saveAll(weathersToSave);
            }
        };
    }

    /**
     * 주기적으로 날씨 수집 Job을 실행하는 스케줄러입니다.
     */
    @Scheduled(cron = "0 10 2,5,8,11,14,17,20,23 * * *", zone = "Asia/Seoul")
    public void runFetchWeatherJob() {
        try {
            JobParameters params = new JobParametersBuilder()
                    .addString("scheduledTime", String.valueOf(System.currentTimeMillis()))
                    .toJobParameters();
            jobLauncher.run(fetchWeatherJob(jobRepository, fetchWeatherStep(jobRepository, transactionManager, profileItemReader(), weatherItemProcessor(), weatherListItemWriter())), params);
            log.info("Scheduled weather fetch job has been run successfully.");
        } catch (Exception e) {
            log.error("Failed to run scheduled weather fetch job", e);
        }
    }
}
