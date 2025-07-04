package com.codeit.otboo.domain.weather.batch;

import com.codeit.otboo.domain.weather.component.KmaApiClient;
import com.codeit.otboo.domain.weather.component.WeatherParser;
import com.codeit.otboo.domain.weather.dto.WeatherDto;
import com.codeit.otboo.domain.weather.entity.Weather;
import com.codeit.otboo.domain.weather.entity.vo.LocationInfo;
import com.codeit.otboo.domain.weather.repository.WeatherRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Configuration
@EnableBatchProcessing
@EnableScheduling      // 스케줄링 기능 활성화
@RequiredArgsConstructor
public class WeatherBatchConfig {

    private final KmaApiClient kmaApiClient;
    private final WeatherParser weatherParser;
    private final WeatherRepository weatherRepository;
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final JobLauncher jobLauncher; // 스케줄러에서 Job을 실행시키기 위해 주입받습니다.

    // 1. 날씨 수집 Job 정의
    @Bean
    public Job fetchWeatherJob() {
        return new JobBuilder("fetchWeatherJob", jobRepository)
                .start(fetchWeatherStep())
                .build();
    }

    // 2. 날씨 수집 Step 정의
    @Bean
    public Step fetchWeatherStep() {
        return new StepBuilder("fetchWeatherStep", jobRepository)
                .<LocationInfo, List<Weather>>chunk(10, transactionManager)
                .reader(locationInfoItemReader())
                .processor(weatherItemProcessor())
                .writer(weatherListItemWriter())
                .build();
    }

    // 3. ItemReader 구현
    @Bean
    public ItemReader<LocationInfo> locationInfoItemReader() {
        // TODO: 향후 DB에서 사용자 위치 정보를 읽어오도록 수정해야 합니다.
        List<LocationInfo> locations = List.of(
                new LocationInfo(37.5666, 126.9782, 60, 127), // 서울
                new LocationInfo(37.8000, 127.0500, 62, 129)  // 양주
        );
        return new ListItemReader<>(locations);
    }

    // 4. ItemProcessor 구현
    @Bean
    public ItemProcessor<LocationInfo, List<Weather>> weatherItemProcessor() {
        return locationInfo -> {
            String rawData = kmaApiClient.fetchWeatherForecast(locationInfo.x(), locationInfo.y());
            List<WeatherDto> dtoList = weatherParser.parseAndGroup(rawData, locationInfo.latitude(), locationInfo.longitude());

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

    // 5. ItemWriter 구현
    @Bean
    public ItemWriter<List<Weather>> weatherListItemWriter() {
        return chunk -> {
            List<Weather> weathersToSave = chunk.getItems().stream()
                    .flatMap(List::stream)
                    .collect(Collectors.toList());

            log.info("Saving {} weather entities to the database.", weathersToSave.size());
            weatherRepository.saveAll(weathersToSave);
        };
    }

    /**
     * 주기적으로 날씨 수집 Job을 실행하는 스케줄러입니다.
     * cron: 기상청 단기예보가 발표되는 매 3시간 주기의 10분 후 (02:10, 05:10, ...)에 실행됩니다.
     * zone: 한국 시간대를 기준으로 동작합니다.
     */
    @Scheduled(cron = "0 10 2,5,8,11,14,17,20,23 * * *", zone = "Asia/Seoul")
    public void runFetchWeatherJob() {
        try {
            // 매번 다른 Job Instance를 생성하기 위해 현재 시간을 파라미터로 넘깁니다.
            JobParameters params = new JobParametersBuilder()
                    .addString("scheduledTime", String.valueOf(System.currentTimeMillis()))
                    .toJobParameters();
            jobLauncher.run(fetchWeatherJob(), params);
            log.info("Scheduled weather fetch job has been run successfully.");
        } catch (Exception e) {
            log.error("Failed to run scheduled weather fetch job", e);
        }
    }
}
