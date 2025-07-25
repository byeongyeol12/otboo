package com.codeit.otboo.domain.user.service;

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
public class S3ImageUploadService implements ImageStorageService {

	private final S3Client s3Client;

	@Value("${cloud.aws.s3.bucket}")
	private String bucketName;

	public S3ImageUploadService(S3Client s3Client) {
		this.s3Client = s3Client;
	}

	@Override
	public String upload(MultipartFile image, String folder) {
		if (image.isEmpty()) return null;

		String originalFilename = image.getOriginalFilename();
		String extension = "";
		if (originalFilename != null && originalFilename.contains(".")) {
			extension = originalFilename.substring(originalFilename.lastIndexOf("."));
		}

		String storedFilename = UUID.randomUUID() + extension;
		String key = folder + "/" + storedFilename;

		try {
			PutObjectRequest putObjectRequest = PutObjectRequest.builder()
				.bucket(bucketName)
				.key(key)
				.contentType(image.getContentType())
				.contentLength(image.getSize())
				.build();

			PutObjectResponse response = s3Client.putObject(
				putObjectRequest,
				RequestBody.fromInputStream(image.getInputStream(), image.getSize())
			);

			if (response.sdkHttpResponse().isSuccessful()) {
				return String.format("https://%s.s3.%s.amazonaws.com/%s",
					bucketName,
					s3Client.serviceClientConfiguration().region().id(),
					key);
			} else {
				throw new RuntimeException("S3 업로드 실패: " + response.sdkHttpResponse().statusText().orElse("Unknown"));
			}
		} catch (IOException | S3Exception e) {
			throw new RuntimeException("S3 업로드 오류: " + e.getMessage(), e);
		}
	}
}