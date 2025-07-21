package com.codeit.otboo.domain;

import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * OtbooApplication 이 항상 테스트 컨텍스트에 포함돼
 * 컨트롤러 단위테스트에서 entityManagerFactory 를 찾다 에러 발생
 */
@SpringBootApplication
public class TestApplication {
}
