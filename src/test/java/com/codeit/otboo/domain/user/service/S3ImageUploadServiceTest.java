package com.codeit.otboo.domain.user.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.io.IOException;
import java.net.URI;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.http.SdkHttpResponse;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;

class S3ImageUploadServiceTest {

	@InjectMocks
	private S3UserImageUploadService s3ImageUploadService;

	@Mock
	private S3Client s3Client;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		s3ImageUploadService = new S3UserImageUploadService(s3Client);
		ReflectionTestUtils.setField(s3ImageUploadService, "bucketName", "test-bucket");
	}

	@Test
	@DisplayName("S3 업로드 성공")
	void uploadImage_success() throws IOException {
		// given
		byte[] content = "test-image".getBytes();
		MultipartFile multipartFile = new MockMultipartFile(
			"image", "sample.png", "image/png", content);

		PutObjectResponse mockResponse = mock(PutObjectResponse.class);
		SdkHttpResponse sdkHttpResponse = SdkHttpResponse.builder()
			.statusCode(200)
			.build();

		given(mockResponse.sdkHttpResponse()).willReturn(sdkHttpResponse);
		given(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class))).willReturn(mockResponse);
		given(s3Client.serviceClientConfiguration()).willReturn(S3Client.builder()
			.region(Region.AP_NORTHEAST_2)
			.endpointOverride(URI.create("http://localhost"))
			.build()
			.serviceClientConfiguration());

		// when
		String result = s3ImageUploadService.upload(multipartFile, "profile");

		// then
		assertThat(result).startsWith("https://test-bucket.s3.ap-northeast-2.amazonaws.com/profile/");
		assertThat(result).endsWith(".png");
	}

	@Test
	@DisplayName("비어있는 파일 업로드 - null 반환")
	void uploadImage_emptyFile_returnsNull() {
		// given
		MultipartFile emptyFile = new MockMultipartFile("image", "", "image/png", new byte[0]);

		// when
		String result = s3ImageUploadService.upload(emptyFile, "profile");

		// then
		assertThat(result).isNull();
	}

	@Test
	@DisplayName("IOException 발생 시 예외 처리")
	void uploadImage_throwsIOException() throws IOException {
		// given
		MultipartFile file = mock(MultipartFile.class);
		given(file.isEmpty()).willReturn(false);
		given(file.getOriginalFilename()).willReturn("test.jpg");
		given(file.getContentType()).willReturn("image/jpeg");
		given(file.getSize()).willReturn(123L);
		given(file.getInputStream()).willThrow(new IOException("Stream error"));

		// when & then
		assertThatThrownBy(() -> s3ImageUploadService.upload(file, "profile"))
			.isInstanceOf(RuntimeException.class)
			.hasMessageContaining("S3 업로드 오류");
	}

	@Test
	@DisplayName("S3 putObject 실패 시 예외 처리")
	void uploadImage_putObjectFails() throws IOException {
		// given
		MultipartFile file = new MockMultipartFile(
			"image", "test.jpg", "image/jpeg", "test".getBytes()
		);

		given(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
			.willThrow(S3Exception.builder().message("S3 error").build());

		// when & then
		assertThatThrownBy(() -> s3ImageUploadService.upload(file, "profile"))
			.isInstanceOf(RuntimeException.class)
			.hasMessageContaining("S3 업로드 오류");
	}
}
