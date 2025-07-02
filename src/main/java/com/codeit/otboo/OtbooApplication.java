package com.codeit.otboo;

import io.github.cdimascio.dotenv.Dotenv; // Dotenv 임포트
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.codeit.otboo")
@ComponentScan(basePackages = "com.codeit.otboo")
public class OtbooApplication {

	public static void main(String[] args) {
		// .env 파일을 로드하고 시스템 속성으로 설정합니다.
		Dotenv dotenv = Dotenv.load();
		dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));

		SpringApplication.run(OtbooApplication.class, args);
	}

}