package com.codeit.otboo.domain.clothes.service;

import java.io.IOException;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Service
public class S3ImageUploadService {

	private final S3Client s3Client;

	@Value("${cloud.aws.s3.bucket}") // application.yml 또는 환경 변수에 설정되어야 함
	private String bucketName;

	public S3ImageUploadService(S3Client s3Client) {
		this.s3Client = s3Client;
	}

	public String upload(MultipartFile image) {
		if (image.isEmpty()) {
			return null;
		}

		String originalFilename = image.getOriginalFilename();
		String extension = "";
		if (originalFilename != null && originalFilename.lastIndexOf(".") != -1) {
			extension = originalFilename.substring(originalFilename.lastIndexOf("."));
		}

		String storedFilename = UUID.randomUUID().toString() + extension;
		String s3ObjectKey = "images/" + storedFilename; // S3 버킷 내 'images/' 폴더에 저장

		try {
			PutObjectRequest putObjectRequest = PutObjectRequest.builder()
				.bucket(bucketName)
				.key(s3ObjectKey)
				.contentType(image.getContentType())
				.contentLength(image.getSize())
				.build();

			PutObjectResponse response = s3Client.putObject(putObjectRequest,
				RequestBody.fromInputStream(image.getInputStream(), image.getSize()));

			if (response.sdkHttpResponse().isSuccessful()) {
				return String.format("https://%s.s3.%s.amazonaws.com/%s",
					bucketName,
					s3Client.serviceClientConfiguration().region().id(),
					s3ObjectKey);
			} else {
				throw new RuntimeException(
					"S3 업로드에 실패했습니다: " + response.sdkHttpResponse().statusText().orElse("Unknown S3 error"));
			}
		} catch (IOException e) {
			throw new RuntimeException("이미지 파일을 읽는 중 IO 오류 발생: " + e.getMessage(), e);
		} catch (S3Exception e) {
			throw new RuntimeException("AWS S3 서비스 오류 발생: " + e.awsErrorDetails().errorMessage(), e);
		} catch (Exception e) {
			throw new RuntimeException("이미지 업로드 중 예상치 못한 오류 발생: " + e.getMessage(), e);
		}
	}
}