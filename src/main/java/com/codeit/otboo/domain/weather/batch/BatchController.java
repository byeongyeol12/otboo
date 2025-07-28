package com.codeit.otboo.domain.weather.batch;

import com.codeit.otboo.domain.weather.scheduler.WeatherDataCleanupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 배치 작업을 수동으로 실행시키기 위한 테스트용 컨트롤러입니다.
 */
@Tag(name = "배치 관리 (개발용)", description = "배치 작업을 수동으로 실행하는 API")
@RestController
@RequestMapping("/api/batch")
@RequiredArgsConstructor
public class BatchController {

    private final JobLauncher jobLauncher;
    private final Job fetchWeatherJob;
    private final WeatherDataCleanupService weatherDataCleanupService; // ✨ 의존성 추가

    @Operation(summary = "날씨 수집 배치 수동 실행", description = "날씨 예보를 수집하는 배치 작업을 강제로 실행시킵니다. 테스트 및 긴급 상황용입니다.")
    @ApiResponse(responseCode = "200", description = "배치 작업 실행 요청 성공")
    @PostMapping("/run-weather-job")
    public ResponseEntity<String> runWeatherJob() {
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("timestamp", String.valueOf(System.currentTimeMillis()))
                    .toJobParameters();
            jobLauncher.run(fetchWeatherJob, jobParameters);
            return ResponseEntity.ok("Batch job 'fetchWeatherJob' has been started successfully.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to start batch job: " + e.getMessage());
        }
    }

    // ✨ 아래 API 메서드를 새로 추가합니다.
    @Operation(summary = "오래된 날씨 데이터 삭제 작업 수동 실행", description = "생성된 지 2일이 지난 날씨 예보 데이터를 즉시 삭제합니다.")
    @ApiResponse(responseCode = "200", description = "삭제 작업 실행 성공")
    @PostMapping("/run-cleanup-job")
    public ResponseEntity<String> runCleanupJob() {
        weatherDataCleanupService.cleanupOldWeatherData();
        return ResponseEntity.ok("Weather data cleanup job has been executed successfully.");
    }
}