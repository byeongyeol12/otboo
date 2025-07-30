package com.codeit.otboo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication
@EnableScheduling
public class OtbooApplication {

	public static void main(String[] args) {
		// bring profile
		String activeProfile =
			System.getProperty("spring.profiles.active",
				System.getenv().getOrDefault("SPRING_PROFILES_ACTIVE", "dev"));

		// apply dotenv only in case of dev profile
		if ("dev".equals(activeProfile)) {
			Dotenv.configure()
				.ignoreIfMissing()
				.load()
				.entries()
				.forEach(entry ->
					System.setProperty(entry.getKey(), entry.getValue())
				);
		}

		SpringApplication.run(OtbooApplication.class, args);
	}
}
