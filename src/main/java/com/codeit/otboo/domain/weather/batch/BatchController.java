package com.codeit.otboo.domain.weather.batch;

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
}