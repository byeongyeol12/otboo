package com.codeit.otboo.domain.clothes.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

// 임시
@Service
public class LocalImageUploadService {
	private final String uploadDir = "./uploads/";

	public String upload(MultipartFile image) {
		if (image.isEmpty()) {
			return null;
		}

		String originalFilename = image.getOriginalFilename();
		String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
		String storedFilename = UUID.randomUUID().toString() + extension;
		Path destinationPath = Paths.get(uploadDir + storedFilename);

		try {
			Files.createDirectories(destinationPath.getParent());
			image.transferTo(destinationPath);
		} catch (IOException e) {
			throw new RuntimeException("파일 저장에 실패했습니다.", e);
		}

		return "/uploads/" + storedFilename;
	}
}
