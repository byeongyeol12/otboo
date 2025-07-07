package com.codeit.otboo.domain.weather.batch;

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
@RestController
@RequestMapping("/api/batch")
@RequiredArgsConstructor
public class BatchController {

    private final JobLauncher jobLauncher;
    private final Job fetchWeatherJob;

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
