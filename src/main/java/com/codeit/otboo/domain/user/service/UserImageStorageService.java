package com.codeit.otboo.domain.user.service;

import org.springframework.web.multipart.MultipartFile;

public interface UserImageStorageService {
	String upload(MultipartFile file, String folder);
}