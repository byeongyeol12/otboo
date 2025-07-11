package com.codeit.otboo.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry
			.addResourceHandler("/static/profile/**")
			.addResourceLocations("file:C:/Users/mmm80/codeitproject/sb01-otboo-team04/uploads/profile/");
	}

	@Value("${kakao.rest-api-key}")
	private String kakaoKey;
}
