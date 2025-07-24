package com.codeit.otboo;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

/**
 * OtbooApplication 이 항상 테스트 컨텍스트에 포함돼
 * 컨트롤러 단위테스트에서 entityManagerFactory 를 찾다 에러 발생
 */
@SpringBootApplication(scanBasePackages = {"com.codeit.otboo"})
@EntityScan(basePackages = {"com.codeit.otboo"})
public class TestApplication {

}