package com.codeit.otboo.domain.user.service;

import org.springframework.web.multipart.MultipartFile;

public interface ImageStorageService {
	String upload(MultipartFile file);
}