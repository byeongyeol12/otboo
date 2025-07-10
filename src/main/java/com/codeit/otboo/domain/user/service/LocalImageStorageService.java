package com.codeit.otboo.domain.user.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.codeit.otboo.exception.CustomException;
import com.codeit.otboo.global.error.ErrorCode;

@Service
public class LocalImageStorageService implements ImageStorageService {

	private final Path uploadDir = Paths.get("uploads/profile");

	@Override
	public String upload(MultipartFile file) {
		try {
			Files.createDirectories(uploadDir); // ✅ 디렉토리 없으면 생성
			String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
			Path target = uploadDir.resolve(filename);
			Files.copy(file.getInputStream(), target);
			return "/static/profile/" + filename;
		} catch (IOException e) {
			throw new CustomException(ErrorCode.PROFILE_IMAGE_UPLOAD_FAILED);
		}
	}
}